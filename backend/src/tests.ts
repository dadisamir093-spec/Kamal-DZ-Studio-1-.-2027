import test from 'node:test';
import assert from 'node:assert';
import { QuotaDurableObject } from './durableObject';
import { verifyAppCheckToken } from './appCheck';
import { createHandler, computeFluxCost } from './index';

// Real in-memory SQLite (node:sqlite) behind the SqlStorage-like interface used by the Durable Object.
const sqlite: any = (process as any).getBuiltinModule('node:sqlite');

class SqlStorageAdapter {
  db = new sqlite.DatabaseSync(':memory:');

  exec(query: string, ...params: any[]) {
    if (/^\s*SELECT/i.test(query)) {
      const rows = this.db.prepare(query).all(...params);
      return { toArray: () => rows };
    }
    if (params.length === 0) {
      this.db.exec(query);
    } else {
      this.db.prepare(query).run(...params);
    }
    return { toArray: () => [] };
  }
}

class MockDurableObjectState {
  storage = { sql: new SqlStorageAdapter() };
}

const DAY = 24 * 60 * 60 * 1000;

function newDo(env: any = {}) {
  const state = new MockDurableObjectState();
  const instance = new QuotaDurableObject(state as any, env);
  return { state, instance };
}

function post(instance: QuotaDurableObject, path: string, body: any) {
  return instance.fetch(new Request(`https://do${path}`, { method: 'POST', body: JSON.stringify(body) }));
}

function usedOn(state: MockDurableObjectState, date: string): number {
  const rows = state.storage.sql.exec(`SELECT used_neurons FROM daily_quota WHERE date = ?`, date).toArray();
  return rows.length ? Number(rows[0].used_neurons) : 0;
}

function dateOf(ms: number) {
  return new Date(ms).toISOString().split('T')[0];
}

// Worker harness: fake App Check and a fake AI binding. No real FLUX or network call is ever made.
function makeWorker(opts: { env?: any; aiError?: string } = {}) {
  const { state, instance } = newDo(opts.env || {});
  const calls = { flux: 0, translate: 0 };
  const env: any = {
    ...(opts.env || {}),
    AI: {
      run: async (model: string) => {
        if (model.includes('flux')) {
          calls.flux++;
          if (opts.aiError) throw new Error(opts.aiError);
          return { image: btoa('fake-jpeg-bytes') };
        }
        calls.translate++;
        return { translated_text: 'translated' };
      },
    },
    QUOTA_MANAGER: {
      idFromName: (n: string) => n,
      get: () => ({ fetch: (req: Request) => instance.fetch(req) }),
    },
  };
  const handler = createHandler(async (header) => header === 'Bearer good'
    ? { isValid: true, appId: 'app' }
    : { isValid: false, error: 'secret internal detail' });
  const generate = (requestId?: string, body: any = { prompt: 'a cat', width: 1024, height: 576, steps: 4 }) =>
    handler.fetch(new Request('https://w/ai/generate-image', {
      method: 'POST',
      headers: { Authorization: 'Bearer good', ...(requestId ? { 'X-Request-ID': requestId } : {}) },
      body: JSON.stringify(body),
    }), env);
  return { state, instance, calls, env, handler, generate };
}

// ---- Original tests (kept) ----

test('QuotaDurableObject reserve and complete success', async () => {
  const { instance } = newDo();

  const res1 = await post(instance, '/reserve', { requestId: 'req-123', cost: 200 });
  const data1 = await res1.json() as any;
  assert.strictEqual(data1.allowed, true);
  assert.strictEqual(data1.reservedNeurons, 200);

  const resComp = await post(instance, '/complete', { requestId: 'req-123', success: true });
  const dataComp = await resComp.json() as any;
  assert.strictEqual(dataComp.status, 'SUCCEEDED');
});

test('QuotaDurableObject failure refund and UNKNOWN_OUTCOME', async () => {
  const { instance } = newDo();

  await post(instance, '/reserve', { requestId: 'req-456', cost: 200 });
  const dataUnk = await (await post(instance, '/unknown', { requestId: 'req-456' })).json() as any;
  assert.strictEqual(dataUnk.status, 'UNKNOWN_OUTCOME');

  await post(instance, '/reserve', { requestId: 'req-789', cost: 200 });
  const dataFail = await (await post(instance, '/complete', { requestId: 'req-789', success: false })).json() as any;
  assert.strictEqual(dataFail.status, 'FAILED_CONFIRMED');
});

test('AppCheck validation unit test with invalid tokens', async () => {
  const res1 = await verifyAppCheckToken(null, 'project-id', 'app-id');
  assert.strictEqual(res1.isValid, false);

  const res2 = await verifyAppCheckToken('Bearer invalid.jwt.token', 'project-id', 'app-id');
  assert.strictEqual(res2.isValid, false);
});

test('FLUX official cost calculation verification', () => {
  const steps = 4;
  assert.strictEqual(Math.ceil((steps * 9.6) + (1 * 4.8)), 44);
  assert.strictEqual(Math.ceil((steps * 9.6) + (4 * 4.8)), 58);
  assert.strictEqual(Math.ceil((steps * 9.6) + (4 * 4.8)), 58);
  assert.strictEqual(computeFluxCost(512, 512, 4, 200), 44);
  assert.strictEqual(computeFluxCost(1024, 576, 4, 200), 58);
});

// ---- New tests: Request ID reuse ----

test('new request ID reserves quota and runs FLUX once', async () => {
  const w = makeWorker();
  const res = await w.generate('req-new-0001');
  assert.strictEqual(res.status, 200);
  assert.strictEqual(w.calls.flux, 1);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), 58);
});

test('repeat of a RESERVED / RUNNING request ID returns 409 and does not run FLUX', async () => {
  const w = makeWorker();
  await post(w.instance, '/reserve', { requestId: 'req-busy-0001', cost: 58 });
  const before = usedOn(w.state, dateOf(Date.now()));

  const res = await w.generate('req-busy-0001');
  assert.strictEqual(res.status, 409);
  assert.strictEqual(w.calls.flux, 0);

  w.state.storage.sql.exec(`UPDATE request_states SET status = ? WHERE request_id = ?`, 'RUNNING', 'req-busy-0001');
  const res2 = await w.generate('req-busy-0001');
  assert.strictEqual(res2.status, 409);
  assert.strictEqual(w.calls.flux, 0);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), before);
});

test('repeat after SUCCEEDED returns 409, no second FLUX run, no second charge', async () => {
  const w = makeWorker();
  assert.strictEqual((await w.generate('req-done-0001')).status, 200);
  const used = usedOn(w.state, dateOf(Date.now()));

  const res = await w.generate('req-done-0001');
  assert.strictEqual(res.status, 409);
  assert.strictEqual(w.calls.flux, 1);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), used);
});

test('repeat after FAILED returns 409, no FLUX re-run, refund applied once', async () => {
  const w = makeWorker({ aiError: 'model exploded' });
  const first = await w.generate('req-fail-0001');
  assert.strictEqual(first.status, 502);
  assert.strictEqual(w.calls.flux, 1);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), 0);

  const res = await w.generate('req-fail-0001');
  assert.strictEqual(res.status, 409);
  assert.strictEqual(w.calls.flux, 1);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), 0);
});

test('UNKNOWN_OUTCOME keeps quota, is never refunded and is never re-run with the same ID', async () => {
  const w = makeWorker({ aiError: 'request timeout' });
  const first = await w.generate('req-unk-0001');
  assert.strictEqual(first.status, 502);
  const today = dateOf(Date.now());
  assert.strictEqual(usedOn(w.state, today), 58);

  // A late "failed" completion must not refund an unconfirmed outcome.
  await post(w.instance, '/complete', { requestId: 'req-unk-0001', success: false });
  assert.strictEqual(usedOn(w.state, today), 58);

  const res = await w.generate('req-unk-0001');
  assert.strictEqual(res.status, 409);
  assert.strictEqual(w.calls.flux, 1);
  assert.strictEqual(usedOn(w.state, today), 58);
});

// ---- New tests: limits and dates ----

test('daily limit comes from env.DAILY_NEURON_LIMIT', async () => {
  const w = makeWorker({ env: { DAILY_NEURON_LIMIT: '100' } });
  assert.strictEqual((await w.generate('req-lim-0001')).status, 200); // 58 <= 100
  const second = await w.generate('req-lim-0002'); // 116 > 100
  assert.strictEqual(second.status, 429);
  assert.strictEqual(w.calls.flux, 1);

  const info = await (await w.handler.fetch(new Request('https://w/ai/quota-info'), w.env)).json() as any;
  assert.strictEqual(info.dailyLimit, 100);
  assert.strictEqual(info.remainingNeurons, 42);

  const def = newDo({});
  assert.strictEqual(def.instance.dailyLimit, 10000);
  const bad = newDo({ DAILY_NEURON_LIMIT: 'abc' });
  assert.strictEqual(bad.instance.dailyLimit, 10000);
});

test('refund uses the reservation day, not the day of completion', async () => {
  const { state, instance } = newDo();
  const realNow = Date.now;
  const day1 = Date.UTC(2026, 0, 10, 23, 59, 0);
  const day2 = day1 + 2 * 60 * 1000;
  try {
    Date.now = () => day1;
    await post(instance, '/reserve', { requestId: 'req-mid-0001', cost: 100 });
    Date.now = () => day2;
    await post(instance, '/reserve', { requestId: 'req-mid-0002', cost: 50 });
    assert.strictEqual(usedOn(state, '2026-01-10'), 100);
    assert.strictEqual(usedOn(state, '2026-01-11'), 50);

    await post(instance, '/complete', { requestId: 'req-mid-0001', success: false });
    assert.strictEqual(usedOn(state, '2026-01-10'), 0);
    assert.strictEqual(usedOn(state, '2026-01-11'), 50);
  } finally {
    Date.now = realNow;
  }
});

test('old request records are cleaned up after the retention window', async () => {
  const { state, instance } = newDo();
  const realNow = Date.now;
  const t0 = Date.UTC(2026, 0, 1, 12, 0, 0);
  try {
    Date.now = () => t0;
    await post(instance, '/reserve', { requestId: 'req-old-0001', cost: 10 });
    Date.now = () => t0 + 8 * DAY;
    await post(instance, '/reserve', { requestId: 'req-new-0002', cost: 10 });
    const rows = state.storage.sql.exec(`SELECT request_id FROM request_states`).toArray();
    assert.deepStrictEqual(rows.map((r: any) => r.request_id), ['req-new-0002']);
  } finally {
    Date.now = realNow;
  }
});

test('concurrent generations above the cap are rejected without FLUX', async () => {
  const w = makeWorker({ env: { MAX_CONCURRENT_GENERATIONS: '2' } });
  await post(w.instance, '/reserve', { requestId: 'req-par-0001', cost: 58 });
  await post(w.instance, '/reserve', { requestId: 'req-par-0002', cost: 58 });
  const res = await w.generate('req-par-0003');
  assert.strictEqual(res.status, 429);
  assert.strictEqual(w.calls.flux, 0);
});

// ---- New tests: security ----

test('invalid or missing App Check token is rejected before any AI or quota use', async () => {
  const w = makeWorker();
  const handler = createHandler(verifyAppCheckToken);
  const bad = await handler.fetch(new Request('https://w/ai/generate-image', {
    method: 'POST',
    headers: { Authorization: 'Bearer invalid.jwt.token', 'X-Request-ID': 'req-sec-0001' },
    body: JSON.stringify({ prompt: 'x' }),
  }), w.env);
  assert.strictEqual(bad.status, 401);
  const badBody = JSON.stringify(await bad.json());
  assert.ok(!badBody.includes('JWT'), 'must not leak verification details');

  const missing = await handler.fetch(new Request('https://w/ai/generate-image', {
    method: 'POST',
    headers: { 'X-Request-ID': 'req-sec-0002' },
    body: JSON.stringify({ prompt: 'x' }),
  }), w.env);
  assert.strictEqual(missing.status, 401);

  assert.strictEqual(w.calls.flux, 0);
  assert.strictEqual(w.calls.translate, 0);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), 0);
});

test('quota-status and health leak no sensitive data', async () => {
  const w = makeWorker();
  await w.generate('req-leak-0001');

  const noAuth = await w.handler.fetch(new Request('https://w/ai/quota-status?requestId=req-leak-0001'), w.env);
  assert.strictEqual(noAuth.status, 401);

  const ok = await w.handler.fetch(new Request('https://w/ai/quota-status?requestId=req-leak-0001', {
    headers: { Authorization: 'Bearer good' },
  }), w.env);
  const data = await ok.json() as any;
  assert.deepStrictEqual(Object.keys(data), ['status']);
  assert.strictEqual(data.status, 'SUCCEEDED');

  const health = await (await w.handler.fetch(new Request('https://w/health'), w.env)).json() as any;
  assert.deepStrictEqual(Object.keys(health), ['status']);
});

test('reset-quota stays hidden without a configured and matching ADMIN_TOKEN', async () => {
  const w = makeWorker();
  const none = await w.handler.fetch(new Request('https://w/ai/reset-quota', { method: 'POST', headers: { 'X-Admin-Token': 'x' } }), w.env);
  assert.strictEqual(none.status, 404);

  const withToken = { ...w.env, ADMIN_TOKEN: 'secret-test-token' };
  const wrong = await w.handler.fetch(new Request('https://w/ai/reset-quota', { method: 'POST', headers: { 'X-Admin-Token': 'nope' } }), withToken);
  assert.strictEqual(wrong.status, 404);
  const right = await w.handler.fetch(new Request('https://w/ai/reset-quota', { method: 'POST', headers: { 'X-Admin-Token': 'secret-test-token' } }), withToken);
  assert.strictEqual(right.status, 200);
});

test('tests never call real FLUX: AI binding is a local fake', async () => {
  const w = makeWorker();
  await w.generate('req-fake-0001');
  assert.strictEqual(w.calls.flux, 1);
  assert.strictEqual(typeof (globalThis as any).fetch, 'function');
});

test('oversized prompt is rejected before any quota or AI use', async () => {
  const w = makeWorker();
  const res = await w.generate('req-long-0001', { prompt: 'a'.repeat(2001) });
  assert.strictEqual(res.status, 400);
  assert.strictEqual(w.calls.flux, 0);
  assert.strictEqual(usedOn(w.state, dateOf(Date.now())), 0);
});

test('image is still returned when the success bookkeeping call fails', async () => {
  const w = makeWorker();
  const realFetch = w.env.QUOTA_MANAGER.get;
  w.env.QUOTA_MANAGER.get = () => ({
    fetch: (req: Request) => {
      if (new URL(req.url).pathname === '/complete') throw new Error('storage down');
      return w.instance.fetch(req);
    },
  });
  const res = await w.generate('req-book-0001');
  assert.strictEqual(res.status, 200);
  assert.strictEqual(w.calls.flux, 1);
  w.env.QUOTA_MANAGER.get = realFetch;
  assert.strictEqual((await w.generate('req-book-0001')).status, 409);
  assert.strictEqual(w.calls.flux, 1);
});
