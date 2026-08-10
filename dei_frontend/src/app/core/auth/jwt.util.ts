export interface JwtPayload {
  sub?: string;
  uid?: number;
  eid?: string;
  role?: string;
  exp?: number;
  iat?: number;
  [k: string]: unknown;
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const segments = token.split('.');
    if (segments.length < 2) return null;
    const base64Payload = segments[1].replace(/-/g, '+').replace(/_/g, '/');
    const paddedPayload = base64Payload + '='.repeat((4 - (base64Payload.length % 4)) % 4);
    const decodedJson = decodeURIComponent(
      atob(paddedPayload)
        .split('')
        .map((char) => '%' + ('00' + char.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );
    return JSON.parse(decodedJson) as JwtPayload;
  } catch {
    return null;
  }
}

export function isTokenExpired(token: string | null): boolean {
  if (!token) return true;
  const payload = decodeJwt(token);
  if (!payload?.exp) return false;
  const nowSec = Math.floor(Date.now() / 1000);
  return payload.exp <= nowSec;
}
