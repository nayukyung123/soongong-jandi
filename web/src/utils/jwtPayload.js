/**
 * 표시용으로만 JWT 페이로드를 읽습니다 (서명 검증 없음).
 * @param {string | null | undefined} token
 * @returns {Record<string, unknown> | null}
 */
export function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') {
    return null;
  }
  try {
    const part = token.split('.')[1];
    if (!part) {
      return null;
    }
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}
