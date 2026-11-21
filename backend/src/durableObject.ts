export interface Env {
  AI: any;
  QUOTA_MANAGER: DurableObjectNamespace;
  DAILY_NEURON_LIMIT: string;
  FLUX_CONSERVATIVE_COST: string;
  FIREBASE_PROJECT_ID: string;
  FIREBASE_APP_ID: string;
}

export class QuotaDurableObject {
  state: DurableObjectState;
  sql: SqlStorage;

  constructor(state: DurableObjectState, env: Env) {
    this.state = state;
    this.sql = state.storage.sql;
    this.initDatabase();
  }

  private initDatabase() {
    this.sql.exec(`
      CREATE TABLE IF NOT EXISTS daily_quota (
        date TEXT PRIMARY KEY,
        used_neurons INTEGER NOT NULL DEFAULT 0
      );
    `);

    this.sql.exec(`
      CREATE TABLE IF NOT EXISTS request_states (
        request_id TEXT PRIMARY KEY,
        status TEXT NOT NULL,
        neurons_reserved INTEGER NOT NULL,
        created_at INTEGER NOT NULL
      );
    `);
  }

  private getTodayUTC(): string {
    return new Date().toISOString().split('T')[0];
  }

  async fetch(request: Request): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname;

    try {
      if (path === '/reserve') {
        const body = await request.json() as { requestId: string; cost: number };
        return this.handleReserve(body.requestId, body.cost);
      } else if (path === '/complete') {
        const body = await request.json() as { requestId: string; success: boolean };
        return this.handleComplete(body.requestId, body.success);
      } else if (path === '/unknown') {
        const body = await request.json() as { requestId: string };
        return this.handleUnknown(body.requestId);
      } else if (path === '/status') {
        const requestId = url.searchParams.get('requestId');
        return this.handleStatus(requestId);
      } else if (path === '/quota-info') {
        return this.handleQuotaInfo();
      } else if (path === '/reset') {
        return this.handleReset();
      }
      return new Response('Not found', { status: 404 });
    } catch (err: any) {
      // Fail Closed on any DB or storage exception
      return Response.json({ allowed: false, error: `Fail-closed storage error: ${err.message}` }, { status: 500 });
    }
  }

  private handleReserve(requestId: string, cost: number): Response {
    if (!requestId) {
      return Response.json({ allowed: false, error: 'Missing requestId' }, { status: 400 });
    }

    const today = this.getTodayUTC();
    const limit = 10000; // DAILY_NEURON_LIMIT

    // Check if requestId already exists (Idempotency)
    const existing = this.sql.exec(`SELECT status, neurons_reserved FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (existing.length > 0) {
      const row = existing[0];
      return Response.json({ 
        allowed: row.status === 'RESERVED' || row.status === 'RUNNING' || row.status === 'SUCCEEDED' || row.status === 'UNKNOWN_OUTCOME',
        status: row.status,
        message: 'Duplicate request idempotent check'
      });
    }

    // Get current usage
    const quotaRows = this.sql.exec(`SELECT used_neurons FROM daily_quota WHERE date = ?`, today).toArray();
    let currentUsed = 0;
    if (quotaRows.length > 0) {
      currentUsed = Number(quotaRows[0].used_neurons);
    }

    if (currentUsed + cost > limit) {
      return Response.json({ 
        allowed: false, 
        error: `Quota exceeded: current usage ${currentUsed} + cost ${cost} exceeds daily limit ${limit}` 
      }, { status: 429 });
    }

    // Perform atomic reservation
    if (quotaRows.length === 0) {
      this.sql.exec(`INSERT INTO daily_quota (date, used_neurons) VALUES (?, ?)`, today, cost);
    } else {
      this.sql.exec(`UPDATE daily_quota SET used_neurons = used_neurons + ? WHERE date = ?`, cost, today);
    }

    this.sql.exec(
      `INSERT INTO request_states (request_id, status, neurons_reserved, created_at) VALUES (?, ?, ?, ?)`,
      requestId, 'RESERVED', cost, Date.now()
    );

    return Response.json({ allowed: true, reservedNeurons: cost });
  }

  private handleComplete(requestId: string, success: boolean): Response {
    const rows = this.sql.exec(`SELECT status, neurons_reserved FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (rows.length === 0) {
      return Response.json({ error: 'Request not found' }, { status: 404 });
    }

    const row = rows.rows ? rows.rows[0] : rows[0];
    const status = row.status as string;
    const reserved = Number(row.neurons_reserved);

    if (status === 'SUCCEEDED' || status === 'FAILED_CONFIRMED') {
      return Response.json({ status, message: 'Already finalized' });
    }

    const newStatus = success ? 'SUCCEEDED' : 'FAILED_CONFIRMED';
    this.sql.exec(`UPDATE request_states SET status = ? WHERE request_id = ?`, newStatus, requestId);

    if (!success) {
      // If explicitly confirmed failed, refund the reserved neurons back to daily quota
      const today = this.getTodayUTC();
      this.sql.exec(`UPDATE daily_quota SET used_neurons = max(0, used_neurons - ?) WHERE date = ?`, reserved, today);
    }

    return Response.json({ status: newStatus });
  }

  private handleUnknown(requestId: string): Response {
    const rows = this.sql.exec(`SELECT status, neurons_reserved FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (rows.length === 0) {
      return Response.json({ error: 'Request not found' }, { status: 404 });
    }

    const row = rows.rows ? rows.rows[0] : rows[0];
    const status = row.status as string;

    if (status === 'SUCCEEDED' || status === 'FAILED_CONFIRMED') {
      return Response.json({ status, message: 'Already finalized' });
    }

    // Mark as UNKNOWN_OUTCOME and DO NOT refund quota automatically on timeout/interruption
    this.sql.exec(`UPDATE request_states SET status = 'UNKNOWN_OUTCOME' WHERE request_id = ?`, requestId);

    return Response.json({ status: 'UNKNOWN_OUTCOME', message: 'Marked as unknown outcome; quota retained for safety.' });
  }

  private handleStatus(requestId: string | null): Response {
    if (!requestId) return Response.json({ error: 'Missing requestId' }, { status: 400 });
    const rows = this.sql.exec(`SELECT status, neurons_reserved, created_at FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (rows.length === 0) {
      return Response.json({ status: 'NOT_FOUND' });
    }
    return Response.json(rows[0]);
  }

  private handleQuotaInfo(): Response {
    const today = this.getTodayUTC();
    const limit = 10000;
    const quotaRows = this.sql.exec(`SELECT used_neurons FROM daily_quota WHERE date = ?`, today).toArray();
    const used = quotaRows.length > 0 ? Number(quotaRows[0].used_neurons) : 0;
    const remaining = Math.max(0, limit - used);
    return Response.json({ date: today, usedNeurons: used, dailyLimit: limit, remainingNeurons: remaining });
  }

  private handleReset(): Response {
    const today = this.getTodayUTC();
    this.sql.exec(`UPDATE daily_quota SET used_neurons = 0 WHERE date = ?`, today);
    this.sql.exec(`DELETE FROM request_states`);
    return Response.json({ success: true, message: 'Quota and state reset for testing' });
  }
}
