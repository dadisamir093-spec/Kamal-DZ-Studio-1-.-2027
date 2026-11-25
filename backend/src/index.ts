import { verifyAppCheckToken } from './appCheck';
import { parsePositiveInt, DEFAULT_FLUX_CONSERVATIVE_COST } from './durableObject';
import type { Env } from './durableObject';
export { QuotaDurableObject } from './durableObject';
export type { Env };

type VerifyFn = typeof verifyAppCheckToken;

const REQUEST_ID_PATTERN = /^[A-Za-z0-9_-]{8,128}$/;
const MAX_PROMPT_LENGTH = 2000;

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Firebase-AppCheck, X-Request-ID, X-Admin-Token',
};

export function computeFluxCost(width: unknown, height: unknown, steps: unknown, fallbackCost: number): number {
  // Dynamic cost calculation for FLUX.1 [schnell] based on official Cloudflare pricing:
  // Cost = (num_steps * 9.60) + (num_tiles * 4.80) Neurons
  const reqWidth = Math.min(Math.max(Number(width) || 1024, 256), 1024);
  const reqHeight = Math.min(Math.max(Number(height) || 576, 256), 1024);
  const reqSteps = Math.min(Math.max(Number(steps) || 4, 1), 4); // Capped at 4 for schnell
  const tiles = Math.ceil(reqWidth / 512) * Math.ceil(reqHeight / 512);
  const cost = Math.ceil((reqSteps * 9.60) + (tiles * 4.80));
  return Number.isFinite(cost) && cost > 0 ? cost : fallbackCost;
}

async function safeEqual(a: string, b: string): Promise<boolean> {
  const enc = new TextEncoder();
  const [ha, hb] = await Promise.all([
    crypto.subtle.digest('SHA-256', enc.encode(a)),
    crypto.subtle.digest('SHA-256', enc.encode(b)),
  ]);
  const va = new Uint8Array(ha);
  const vb = new Uint8Array(hb);
  let diff = 0;
  for (let i = 0; i < va.length; i++) diff |= va[i] ^ vb[i];
  return diff === 0;
}

export function createHandler(verify: VerifyFn = verifyAppCheckToken) {
  const checkAppCheck = async (request: Request, env: Env): Promise<Response | null> => {
    const authHeader = request.headers.get('Authorization') || request.headers.get('X-Firebase-AppCheck');
    const verification = await verify(
      authHeader,
      env.FIREBASE_PROJECT_ID || 'gen-lang-client-0522781997',
      env.FIREBASE_APP_ID || '1:794275757471:android:b2354bd07c0d8094a291aa'
    );
    if (!verification.isValid) {
      return Response.json({ error: 'Unauthorized: invalid or missing App Check token' }, { status: 401, headers: corsHeaders });
    }
    return null;
  };

  return {
    async fetch(request: Request, env: Env, _ctx?: ExecutionContext): Promise<Response> {
      const url = new URL(request.url);
      const path = url.pathname;

      if (request.method === 'OPTIONS') {
        return new Response(null, { headers: corsHeaders });
      }

      if (path === '/health') {
        return Response.json({ status: 'ok' }, { headers: corsHeaders });
      }

      // Security & App Check Verification (Mandatory for all AI endpoints)
      if (path === '/ai/generate-image') {
        if (request.method !== 'POST') {
          return Response.json({ error: 'Method not allowed' }, { status: 405, headers: corsHeaders });
        }

        const denied = await checkAppCheck(request, env);
        if (denied) return denied;

        // Request ID (idempotency key): single-use
        const requestId = request.headers.get('X-Request-ID') || crypto.randomUUID();
        if (!REQUEST_ID_PATTERN.test(requestId)) {
          return Response.json({ error: 'Invalid X-Request-ID' }, { status: 400, headers: corsHeaders });
        }

        let body: { prompt?: unknown; steps?: unknown; width?: unknown; height?: unknown };
        try {
          body = await request.json() as typeof body;
        } catch (_e) {
          return Response.json({ error: 'Invalid JSON body' }, { status: 400, headers: corsHeaders });
        }
        let prompt = typeof body?.prompt === 'string' ? body.prompt : '';
        if (!prompt) {
          return Response.json({ error: 'Missing prompt for image generation' }, { status: 400, headers: corsHeaders });
        }
        if (prompt.length > MAX_PROMPT_LENGTH) {
          return Response.json({ error: `Prompt too long (max ${MAX_PROMPT_LENGTH} characters)` }, { status: 400, headers: corsHeaders });
        }

        const fallbackCost = parsePositiveInt(env.FLUX_CONSERVATIVE_COST, DEFAULT_FLUX_CONSERVATIVE_COST);
        const cost = computeFluxCost(body.width, body.height, body.steps, fallbackCost);
        const reqSteps = Math.min(Math.max(Number(body.steps) || 4, 1), 4);

        const stub = env.QUOTA_MANAGER.get(env.QUOTA_MANAGER.idFromName('global_quota_manager'));

        try {
          // Reserve quota atomically BEFORE any AI call (translation or FLUX)
          const reserveRes = await stub.fetch(new Request('https://do/reserve', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ requestId, cost })
          }));
          const reserveData = await reserveRes.json().catch(() => ({})) as { allowed?: boolean; error?: string };

          if (reserveRes.status === 409) {
            return Response.json({ error: 'Duplicate request ID: this request was already submitted' }, { status: 409, headers: corsHeaders });
          }
          if (reserveRes.status === 429) {
            return Response.json({ error: reserveData.error || 'Daily neuron limit reached. Zero-billing protection activated.' }, { status: 429, headers: corsHeaders });
          }
          if (!reserveRes.ok || reserveData.allowed !== true) {
            return Response.json({ error: 'Request not allowed' }, { status: reserveRes.status === 400 ? 400 : 503, headers: corsHeaders });
          }

          // Translate Arabic prompts to English (FLUX understands English prompts)
          if (/[\u0600-\u06FF]/.test(prompt)) {
            try {
              const tr: any = await env.AI.run('@cf/meta/m2m100-1.2b', { text: prompt, source_lang: 'ar', target_lang: 'en' });
              if (tr && typeof tr.translated_text === 'string' && tr.translated_text.trim()) {
                prompt = tr.translated_text.trim();
              }
            } catch (_e) {
              // keep the original prompt if translation fails
            }
          }

          // Execute FLUX.1 [schnell] via Cloudflare Workers AI Binding
          let imageResult: any;
          try {
            imageResult = await env.AI.run('@cf/black-forest-labs/flux-1-schnell', {
              prompt: prompt,
              steps: reqSteps,
            });
          } catch (aiErr: any) {
            const errMsg = String(aiErr?.message || '');
            const isTimeoutOrNetworkError = errMsg.includes('timeout') || errMsg.includes('abort') || errMsg.includes('interrupted');

            if (isTimeoutOrNetworkError) {
              // Mark as UNKNOWN_OUTCOME: retain quota for safety on timeout/interruption
              await stub.fetch(new Request('https://do/unknown', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId })
              }));
            } else {
              // Complete transaction as failed (triggers refund for confirmed failure)
              await stub.fetch(new Request('https://do/complete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ requestId, success: false })
              }));
            }

            return Response.json({ error: 'Image generation failed' }, { status: 502, headers: corsHeaders });
          }

          // Complete transaction as successful. The image already exists and was charged, so a
          // bookkeeping failure here must not discard it.
          try {
            await stub.fetch(new Request('https://do/complete', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ requestId, success: true })
            }));
          } catch (_e) {
            // quota stays reserved; the request ID stays single-use
          }

          let imageBody: any = imageResult;
          if (typeof imageResult?.image === "string") {
            const bin = atob(imageResult.image);
            const bytes = new Uint8Array(bin.length);
            for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
            imageBody = bytes;
          }
          return new Response(imageBody, {
            headers: {
              ...corsHeaders,
              'Content-Type': 'image/jpeg',
              'X-Request-ID': requestId,
            }
          });

        } catch (_err: any) {
          // Fail-Closed on any unhandled error (details are not returned to callers)
          return Response.json({ error: 'Server error' }, { status: 500, headers: corsHeaders });
        }
      }

      // Quota info endpoint for displaying remaining daily quota in Studio 1
      if (path === '/ai/quota-info') {
        const stub = env.QUOTA_MANAGER.get(env.QUOTA_MANAGER.idFromName('global_quota_manager'));
        const res = await stub.fetch(new Request('https://do/quota-info'));
        const data = await res.json();
        return Response.json(data, { headers: corsHeaders });
      }

      // Status of one request (state only), protected by App Check
      if (path === '/ai/quota-status') {
        const denied = await checkAppCheck(request, env);
        if (denied) return denied;
        const stub = env.QUOTA_MANAGER.get(env.QUOTA_MANAGER.idFromName('global_quota_manager'));
        const res = await stub.fetch(new Request('https://do/status?requestId=' + encodeURIComponent(url.searchParams.get('requestId') || '')));
        const data = await res.json();
        return Response.json(data, { status: res.status, headers: corsHeaders });
      }

      if (path === '/ai/reset-quota') {
        // Disabled unless an ADMIN_TOKEN secret is configured and supplied in X-Admin-Token
        const adminToken = env.ADMIN_TOKEN;
        const supplied = request.headers.get('X-Admin-Token');
        if (request.method !== 'POST' || !adminToken || !supplied || !(await safeEqual(supplied, adminToken))) {
          return new Response('Not found', { status: 404, headers: corsHeaders });
        }
        const stub = env.QUOTA_MANAGER.get(env.QUOTA_MANAGER.idFromName('global_quota_manager'));
        const res = await stub.fetch(new Request('https://do/reset', { method: 'POST' }));
        const data = await res.json();
        return Response.json(data, { headers: corsHeaders });
      }

      return new Response('Not found', { status: 404, headers: corsHeaders });
    }
  };
}

export default createHandler();
