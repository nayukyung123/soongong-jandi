import { apiFetch } from './http.js';

/**
 * 명세 예시 기준: POST /api/posts, 본문 { title, content }, 응답 { id }.
 * MASTER 전용·나머지 HTTP 메서드는 일반적인 REST 관례입니다. 백엔드 경로가 다르면 이 파일만 수정하면 됩니다.
 */

/** @param {{ title: string, content: string }} body */
export function createPost(body) {
  return apiFetch('/posts', {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

/** @returns {Promise<unknown>} */
export function listPosts() {
  return apiFetch('/posts', { method: 'GET' });
}

/** @param {number|string} id */
export function getPost(id) {
  return apiFetch(`/posts/${id}`, { method: 'GET' });
}

/** @param {number|string} id @param {Record<string, unknown>} body */
export function updatePost(id, body) {
  return apiFetch(`/posts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
}

/** @param {number|string} id */
export function deletePost(id) {
  return apiFetch(`/posts/${id}`, { method: 'DELETE' });
}
