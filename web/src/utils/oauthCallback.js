/**
 * OAuth 완료 후 프론트로 리다이렉트될 때 쿼리에 실어보내는 액세스 토큰을 저장합니다.
 * 백엔드가 사용하는 쿼리 키 이름이 다르면 이 목록에 추가하세요.
 *
 * @param {{ setToken: (v: string | null) => void }} authStore
 * @returns {boolean} 토큰을 처리했으면 true
 */
export function applyOAuthCallbackFromUrl(authStore) {
  const url = new URL(window.location.href);
  const keys = ['accessToken', 'token', 'access_token'];

  let found = null;
  for (const k of keys) {
    const v = url.searchParams.get(k);
    if (v) {
      found = { where: 'search', key: k, value: v };
      break;
    }
  }

  if (!found && url.hash && url.hash.length > 1) {
    const hashParams = new URLSearchParams(url.hash.startsWith('#') ? url.hash.slice(1) : url.hash);
    for (const k of keys) {
      const v = hashParams.get(k);
      if (v) {
        found = { where: 'hash', key: k, value: v };
        break;
      }
    }
  }

  if (!found) {
    return false;
  }

  authStore.setToken(found.value);

  if (found.where === 'search') {
    url.searchParams.delete(found.key);
    const qs = url.searchParams.toString();
    window.history.replaceState({}, '', `${url.pathname}${qs ? `?${qs}` : ''}${url.hash}` || '/');
  } else {
    window.history.replaceState({}, '', url.pathname + url.search || '/');
  }
  return true;
}
