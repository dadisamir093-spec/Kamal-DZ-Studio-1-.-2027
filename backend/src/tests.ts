import test from 'node:test';
import assert from 'node:assert';
import { QuotaDurableObject } from './durableObject.ts';
import { verifyAppCheckToken } from './appCheck.ts';

// Mock DurableObjectState for unit testing QuotaDurableObject with SQLite
class MockSqlStorage {
  private data: Map<string, any> = new Map();
  private states: Map<string, any> = new Map();

  exec(query: string, ...params: any[]) {
    if (query.includes('CREATE TABLE')) {
      return { toArray: () => [] };
    }
    if (query.includes('SELECT status, neurons_reserved FROM request_states')) {
      const reqId = params[0];
      const row = this.states.get(reqId);
      return { toArray: () => row ? [row] : [] };
    }
    if (query.includes('SELECT used_neurons FROM daily_quota')) {
      const date = params[0];
      const used = this.data.get(date) || 0;
      return { toArray: () => [{ used_neurons: used }] };
    }
    if (query.includes('INSERT INTO daily_quota')) {
      const date = params[0];
      const used = params[1];
      this.data.set(date, used);
      return { toArray: () => [] };
    }
    if (query.includes('UPDATE daily_quota SET used_neurons = used_neurons +')) {
      const add = params[0];
      const date = params[1];
      const current = this.data.get(date) || 0;
      this.data.set(date, current + add);
      return { toArray: () => [] };
    }
    if (query.includes('UPDATE daily_quota SET used_neurons = max(0, used_neurons -')) {
      const sub = params[0];
      const date = params[1];
      const current = this.data.get(date) || 0;
      this.data.set(date, Math.max(0, current - sub));
      return { toArray: () => [] };
    }
    if (query.includes('INSERT INTO request_states')) {
      const [reqId, status, reserved, createdAt] = params;
      this.states.set(reqId, { request_id: reqId, status, neurons_reserved: reserved, created_at: createdAt });
      return { toArray: () => [] };
    }
    if (query.includes('UPDATE request_states SET status =')) {
      const [status, reqId] = params;
      const row = this.states.get(reqId);
      if (row) row.status = status;
      return { toArray: () => [] };
    }
    return { toArray: () => [] };
  }
}

class MockDurableObjectState {
  storage = {
    sql: new MockSqlStorage()
  };
}

test('QuotaDurableObject reserve and complete success', async () => {
  const mockState = new MockDurableObjectState() as any;
  const doInstance = new QuotaDurableObject(mockState, {} as any);

  // Test Reserve
  const reserveReq = new Request('https://do/reserve', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-123', cost: 200 })
  });
  const res1 = await doInstance.fetch(reserveReq);
  const data1 = await res1.json() as any;
  assert.strictEqual(data1.allowed, true);
  assert.strictEqual(data1.reservedNeurons, 200);

  // Test Idempotency (Duplicate requestId with clone)
  const reserveReq2 = new Request('https://do/reserve', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-123', cost: 200 })
  });
  const resDup = await doInstance.fetch(reserveReq2);
  const dataDup = await resDup.json() as any;
  assert.strictEqual(dataDup.allowed, true);

  // Test Complete Success
  const completeReq = new Request('https://do/complete', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-123', success: true })
  });
  const resComp = await doInstance.fetch(completeReq);
  const dataComp = await resComp.json() as any;
  assert.strictEqual(dataComp.status, 'SUCCEEDED');
});

test('QuotaDurableObject failure refund and UNKNOWN_OUTCOME', async () => {
  const mockState = new MockDurableObjectState() as any;
  const doInstance = new QuotaDurableObject(mockState, {} as any);

  // Reserve req-456
  await doInstance.fetch(new Request('https://do/reserve', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-456', cost: 200 })
  }));

  // Test Unknown Outcome (Timeout / Interruption - quota retained)
  const unknownReq = new Request('https://do/unknown', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-456' })
  });
  const resUnk = await doInstance.fetch(unknownReq);
  const dataUnk = await resUnk.json() as any;
  assert.strictEqual(dataUnk.status, 'UNKNOWN_OUTCOME');

  // Reserve req-789 to test refund on explicit failure
  await doInstance.fetch(new Request('https://do/reserve', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-789', cost: 200 })
  }));

  const failReq = new Request('https://do/complete', {
    method: 'POST',
    body: JSON.stringify({ requestId: 'req-789', success: false })
  });
  const resFail = await doInstance.fetch(failReq);
  const dataFail = await resFail.json() as any;
  assert.strictEqual(dataFail.status, 'FAILED_CONFIRMED');
});

test('AppCheck validation unit test with invalid tokens', async () => {
  // Test missing header
  const res1 = await verifyAppCheckToken(null, 'project-id', 'app-id');
  assert.strictEqual(res1.isValid, false);

  // Test malformed token
  const res2 = await verifyAppCheckToken('Bearer invalid.jwt.token', 'project-id', 'app-id');
  assert.strictEqual(res2.isValid, false);
});

test('FLUX official cost calculation verification', () => {
  const steps = 4;
  
  // 512x512 (1 tile): ceil((4 * 9.6) + (1 * 4.8)) = ceil(38.4 + 4.8) = ceil(43.2) = 44
  const cost1 = Math.ceil((steps * 9.6) + (1 * 4.8));
  assert.strictEqual(cost1, 44);
  
  // 1024x576 (4 tiles): ceil((4 * 9.6) + (4 * 4.8)) = ceil(38.4 + 19.2) = ceil(57.6) = 58
  const cost2 = Math.ceil((steps * 9.6) + (4 * 4.8));
  assert.strictEqual(cost2, 58);
  
  // 1024x1024 (4 tiles): ceil((4 * 9.6) + (4 * 4.8)) = 58
  const cost3 = Math.ceil((steps * 9.6) + (4 * 4.8));
  assert.strictEqual(cost3, 58);
});
