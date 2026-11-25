export interface Env {
  AI: any;
  QUOTA_MANAGER: DurableObjectNamespace;
  DAILY_NEURON_LIMIT: string | number;
  FLUX_CONSERVATIVE_COST: string | number;
  MAX_CONCURRENT_GENERATIONS?: string | number;
  ADMIN_TOKEN?: string;
  FIREBASE_PROJECT_ID: string;
  FIREBASE_APP_ID: string;
}

export const DEFAULT_DAILY_NEURON_LIMIT = 10000;
export const DEFAULT_FLUX_CONSERVATIVE_COST = 200;
export const DEFAULT_MAX_CONCURRENT_GENERATIONS = 3;
const IN_FLIGHT_WINDOW_MS = 2 * 60 * 1000;
const RETENTION_MS = 7 * 24 * 60 * 60 * 1000;

export function parsePositiveInt(value: unknown, fallback: number): number {
  if (value === undefined || value === null || value === '') return fallback;
  const n = Number(value);
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : fallback;
}

export class QuotaDurableObject {
  state: DurableObjectState;
  sql: SqlStorage;
  dailyLimit: number;
  maxConcurrent: number;

  constructor(state: DurableObjectState, env: Partial<Env>) {
    this.state = state;
    this.sql = state.storage.sql;
    this.dailyLimit = parsePositiveInt(env?.DAILY_NEURON_LIMIT, DEFAULT_DAILY_NEURON_LIMIT);
    this.maxConcurrent = parsePositiveInt(env?.MAX_CONCURRENT_GENERATIONS, DEFAULT_MAX_CONCURRENT_GENERATIONS);
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

  private dateUTC(ms: number): string {
    return new Date(ms).toISOString().split('T')[0];
  }

  private getTodayUTC(): string {
    return this.dateUTC(Date.now());
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
    } catch (_err: any) {
      // Fail Closed on any DB or storage exception (details are not returned to callers)
      return Response.json({ allowed: false, error: 'Storage error' }, { status: 500 });
    }
  }

  private cleanupOldRecords(now: number) {
    const cutoff = now - RETENTION_MS;
    this.sql.exec(`DELETE FROM request_states WHERE created_at < ?`, cutoff);
    this.sql.exec(`DELETE FROM daily_quota WHERE date < ?`, this.dateUTC(cutoff));
  }

  private handleReserve(requestId: string, cost: number): Response {
    if (!requestId) {
      return Response.json({ allowed: false, error: 'Missing requestId' }, { status: 400 });
    }
    if (!Number.isFinite(cost) || cost <= 0) {
      return Response.json({ allowed: false, error: 'Invalid cost' }, { status: 400 });
    }

    const now = Date.now();
    const today = this.dateUTC(now);
    const limit = this.dailyLimit;

    // A request ID is single-use: any existing record (in any state) is rejected and never re-run.
    const existing = this.sql.exec(`SELECT status, neurons_reserved, created_at FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (existing.length > 0) {
      return Response.json({
        allowed: false,
        duplicate: true,
        status: existing[0].status,
        error: 'Duplicate request ID: this request was already submitted'
      }, { status: 409 });
    }

    this.cleanupOldRecords(now);

    // Block needless concurrent generations (stale RESERVED records older than the window are ignored)
    const inFlight = this.sql.exec(
      `SELECT status, neurons_reserved, created_at FROM request_states WHERE status IN ('RESERVED', 'RUNNING') AND created_at > ?`,
      now - IN_FLIGHT_WINDOW_MS
    ).toArray();
    if (inFlight.length >= this.maxConcurrent) {
      return Response.json({ allowed: false, error: 'Too many concurrent generations, try again shortly' }, { status: 429 });
    }

    const quotaRows = this.sql.exec(`SELECT used_neurons FROM daily_quota WHERE date = ?`, today).toArray();
    let currentUsed = 0;
    if (quotaRows.length > 0) {
      currentUsed = Number(quotaRows[0].used_neurons);
    }

    if (currentUsed + cost > limit) {
      return Response.json({
        allowed: false,
        error: `Daily neuron limit reached (${limit} neurons/day). Zero-billing protection activated.`
      }, { status: 429 });
    }

    if (quotaRows.length === 0) {
      this.sql.exec(`INSERT INTO daily_quota (date, used_neurons) VALUES (?, ?)`, today, cost);
    } else {
      this.sql.exec(`UPDATE daily_quota SET used_neurons = used_neurons + ? WHERE date = ?`, cost, today);
    }

    this.sql.exec(
      `INSERT INTO request_states (request_id, status, neurons_reserved, created_at) VALUES (?, ?, ?, ?)`,
      requestId, 'RESERVED', cost, now
    );

    return Response.json({ allowed: true, reservedNeurons: cost });
  }

  private handleComplete(requestId: string, success: boolean): Response {
    const rows = this.sql.exec(`SELECT status, neurons_reserved, created_at FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (rows.length === 0) {
      return Response.json({ error: 'Request not found' }, { status: 404 });
    }

    const row = rows[0];
    const status = row.status as string;
    const reserved = Number(row.neurons_reserved);
    const reservedDate = this.dateUTC(Number(row.created_at));

    if (status === 'SUCCEEDED' || status === 'FAILED_CONFIRMED') {
      return Response.json({ status, message: 'Already finalized' });
    }

    if (status === 'UNKNOWN_OUTCOME' && !success) {
      // The outcome was never confirmed: never refund automatically.
      return Response.json({ status, message: 'Outcome unknown; quota retained for safety.' });
    }

    const newStatus = success ? 'SUCCEEDED' : 'FAILED_CONFIRMED';
    this.sql.exec(`UPDATE request_states SET status = ? WHERE request_id = ?`, newStatus, requestId);

    if (!success) {
      // Confirmed failure: refund against the day the neurons were reserved, not today.
      this.sql.exec(`UPDATE daily_quota SET used_neurons = max(0, used_neurons - ?) WHERE date = ?`, reserved, reservedDate);
    }

    return Response.json({ status: newStatus });
  }

  private handleUnknown(requestId: string): Response {
    const rows = this.sql.exec(`SELECT status, neurons_reserved, created_at FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (rows.length === 0) {
      return Response.json({ error: 'Request not found' }, { status: 404 });
    }

    const status = rows[0].status as string;

    if (status === 'SUCCEEDED' || status === 'FAILED_CONFIRMED') {
      return Response.json({ status, message: 'Already finalized' });
    }

    // Mark as UNKNOWN_OUTCOME and DO NOT refund quota automatically on timeout/interruption
    this.sql.exec(`UPDATE request_states SET status = ? WHERE request_id = ?`, 'UNKNOWN_OUTCOME', requestId);

    return Response.json({ status: 'UNKNOWN_OUTCOME', message: 'Marked as unknown outcome; quota retained for safety.' });
  }

  private handleStatus(requestId: string | null): Response {
    if (!requestId) return Response.json({ error: 'Missing requestId' }, { status: 400 });
    const rows = this.sql.exec(`SELECT status, neurons_reserved, created_at FROM request_states WHERE request_id = ?`, requestId).toArray();
    if (rows.length === 0) {
      return Response.json({ status: 'NOT_FOUND' });
    }
    return Response.json({ status: rows[0].status });
  }

  private handleQuotaInfo(): Response {
    const today = this.getTodayUTC();
    const limit = this.dailyLimit;
    const quotaRows = this.sql.exec(`SELECT used_neurons FROM daily_quota WHERE date = ?`, today).toArray();
    const used = quotaRows.length > 0 ? Number(quotaRows[0].used_neurons) : 0;
    const remaining = Math.max(0, limit - used);
    return Response.json({ date: today, usedNeurons: used, dailyLimit: limit, remainingNeurons: remaining });
  }

  private handleReset(): Response {
    const today = this.getTodayUTC();
    // Request states are kept so that already-used request IDs can never run FLUX again.
    this.sql.exec(`UPDATE daily_quota SET used_neurons = 0 WHERE date = ?`, today);
    return Response.json({ success: true, message: 'Daily quota counter reset for testing' });
  }
}
