import { verifyAppCheckToken } from './appCheck.ts';
export { QuotaDurableObject } from './durableObject.ts';

export interface Env {
  AI: any;
  QUOTA_MANAGER: DurableObjectNamespace;
  DAILY_NEURON_LIMIT: string;
  FLUX_CONSERVATIVE_COST: string;
  FIREBASE_PROJECT_ID: string;
  FIREBASE_APP_ID: string;
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname;

    // CORS headers for development/testing if needed
    const corsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Firebase-AppCheck, X-Request-ID',
    };

    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: corsHeaders });
    }

    if (path === '/health') {
      return Response.json({ status: 'ok', service: 'kamal-dz-studio-backend', plan: 'Workers Free' }, { headers: corsHeaders });
    }

    // Security & App Check Verification (Mandatory for all AI endpoints)
    if (path === '/ai/generate-image') {
      const authHeader = request.headers.get('Authorization') || request.headers.get('X-Firebase-AppCheck');
      const verification = await verifyAppCheckToken(
        authHeader, 
        env.FIREBASE_PROJECT_ID || 'gen-lang-client-0522781997', 
        env.FIREBASE_APP_ID || '1:794275757471:android:b2354bd07c0d8094a291aa'
      );

      if (!verification.isValid) {
        return Response.json({ error: `Unauthorized App Check: ${verification.error}` }, { status: 401, headers: corsHeaders });
      }

      // Extract Request ID for Idempotency
      const requestId = request.headers.get('X-Request-ID') || crypto.randomUUID();

      try {
        const body = await request.json() as { prompt?: string; steps?: number; width?: number; height?: number };
        const prompt = body.prompt;
        if (!prompt) {
          return Response.json({ error: 'Missing prompt for image generation' }, { status: 400, headers: corsHeaders });
        }

        // Dynamic cost calculation for FLUX.1 [schnell] based on official Cloudflare pricing:
        // Cost = (num_steps * 9.60) + (num_tiles * 4.80) Neurons
        const reqWidth = Math.min(Math.max(body.width || 1024, 256), 1024);
        const reqHeight = Math.min(Math.max(body.height || 576, 256), 1024);
        const reqSteps = Math.min(Math.max(body.steps || 4, 1), 4); // Capped at 4 for schnell
        const tiles = Math.ceil(reqWidth / 512) * Math.ceil(reqHeight / 512);
        const cost = Math.ceil((reqSteps * 9.60) + (tiles * 4.80));

        // Contact Durable Object for Atomic Quota Reservation
        const doId = env.QUOTA_MANAGER.idFromName('global_quota_manager');
        const stub = env.QUOTA_MANAGER.get(doId);

        const reserveRes = await stub.fetch(new Request('https://do/reserve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ requestId, cost })
        }));

        const reserveData = await reserveRes.json() as { allowed: boolean; error?: string };
        if (!reserveData.allowed) {
          return Response.json({ 
            error: reserveData.error || 'Quota exhausted or Daily Neuron Limit reached (10,000 neurons/day). Zero-billing protection activated.' 
          }, { status: 429, headers: corsHeaders });
        }

        // Execute FLUX.1 [schnell] via Cloudflare Workers AI Binding
        let imageResult: any;
        try {
          imageResult = await env.AI.run('@cf/black-forest-labs/flux-1-schnell', {
            prompt: prompt,
            num_steps: reqSteps,
            width: reqWidth,
            height: reqHeight,
          });
        } catch (aiErr: any) {
          const errMsg = aiErr.message || '';
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

          return Response.json({ error: `Workers AI execution failed: ${aiErr.message}` }, { status: 502, headers: corsHeaders });
        }

        // Complete transaction as successful
        await stub.fetch(new Request('https://do/complete', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ requestId, success: true })
        }));

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

      } catch (err: any) {
        // Fail-Closed on any unhandled error
        return Response.json({ error: `Fail-closed server error: ${err.message}` }, { status: 500, headers: corsHeaders });
      }
    }

    // Quota info endpoint for displaying remaining daily quota in Studio 1
    if (path === '/ai/quota-info') {
      const doId = env.QUOTA_MANAGER.idFromName('global_quota_manager');
      const stub = env.QUOTA_MANAGER.get(doId);
      const res = await stub.fetch(new Request('https://do/quota-info'));
      const data = await res.json();
      return Response.json(data, { headers: corsHeaders });
    }

    // Admin / test route to check quota status for specific request
    if (path === '/ai/quota-status') {
      const doId = env.QUOTA_MANAGER.idFromName('global_quota_manager');
      const stub = env.QUOTA_MANAGER.get(doId);
      const res = await stub.fetch(new Request('https://do/status?requestId=' + (url.searchParams.get('requestId') || '')));
      const data = await res.json();
      return Response.json(data, { headers: corsHeaders });
    }

    if (path === '/ai/reset-quota') {
      const doId = env.QUOTA_MANAGER.idFromName('global_quota_manager');
      const stub = env.QUOTA_MANAGER.get(doId);
      const res = await stub.fetch(new Request('https://do/reset', { method: 'POST' }));
      const data = await res.json();
      return Response.json(data, { headers: corsHeaders });
    }

    return new Response('Not found', { status: 404, headers: corsHeaders });
  }
};
