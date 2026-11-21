import { createRemoteJWKSet, jwtVerify } from 'jose';

// Firebase App Check JWKS official endpoint
const JWKS_URL = new URL('https://firebaseappcheck.googleapis.com/v1/jwks');
const JWKS = createRemoteJWKSet(JWKS_URL);

export interface AppCheckValidationResult {
  isValid: boolean;
  error?: string;
  appId?: string;
}

export async function verifyAppCheckToken(
  authHeader: string | null,
  expectedProjectId: string,
  expectedAppId: string
): Promise<AppCheckValidationResult> {
  if (!authHeader) {
    return { isValid: false, error: 'Missing Authorization or App Check header' };
  }

  const token = authHeader.startsWith('Bearer ') ? authHeader.substring(7) : authHeader;
  if (!token || token.trim() === '') {
    return { isValid: false, error: 'Empty App Check token' };
  }

  const projectNumber = expectedAppId.split(":")[1];
  try {
    // Verify JWT using Firebase App Check JWKS (RS256)
    const { payload } = await jwtVerify(token, JWKS, {
      issuer: `https://firebaseappcheck.googleapis.com/${projectNumber}`,
      audience: [`projects/${projectNumber}`, `projects/${expectedProjectId}`],
    });

    // Validate 'sub' claim matches Firebase App ID
    const sub = payload.sub;
    if (!sub || sub !== expectedAppId) {
      return { 
        isValid: false, 
        error: `App ID mismatch: expected ${expectedAppId}, got ${sub}` 
      };
    }

    return { isValid: true, appId: sub as string };
  } catch (err: any) {
    return { isValid: false, error: `JWT verification failed: ${err.message}` };
  }
}
