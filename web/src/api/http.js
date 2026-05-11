import { AUTH_TOKEN_KEY } from '../stores/auth.js';

/**
 * 백엔드와 통신할 때 `Authorization: Bearer`, `Content-Type: application/json` 을 맞춥니다.
 * 베이스 URL 은 `VITE_API_BASE_URL`(기본 `/api`)이며, 개발 시 vite 프록시로 localhost:8080 에 연결됩니다.
 *
 * @param {string} path `/posts` 또는 `posts` 형태
 * @param {RequestInit} [options]
 * @returns {Promise<unknown>} JSON 본문(비어 있으면 null)
 */
export async function apiFetch(path, options = {}) {
  const base = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '');
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = `${base}${normalizedPath}`;

  const headers = new Headers(options.headers);

  const hasBody = options.body !== undefined && options.body !== null;
  if (hasBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const stored = localStorage.getItem(AUTH_TOKEN_KEY);
  if (stored) {
    headers.set('Authorization', `Bearer ${stored}`);
  }

  const res = await fetch(url, { ...options, headers });

  const contentType = res.headers.get('Content-Type') || '';
  const isJson = contentType.includes('application/json');
  let data = null;
  if (res.status !== 204 && res.status !== 205) {
    const text = await res.text();
    if (text) {
      data = isJson ? JSON.parse(text) : text;
    }
  }

  if (!res.ok) {
    const err = new Error(res.statusText || `HTTP ${res.status}`);
    err.status = res.status;
    err.body = data;
    throw err;
  }

  return data;
}
