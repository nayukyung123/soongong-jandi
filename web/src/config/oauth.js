/**
 * 소셜 로그인 시작 URL.
 * - Spring Security OAuth2 Client 기본: `/oauth2/authorization/{registrationId}` (registrationId: kakao, google 등)
 * - 백엔드가 `/api/v1/auth/...` 아래에 두면 `VITE_OAUTH_AUTHORIZATION_PATH` 로 조정하거나
 *   `VITE_OAUTH_KAKAO_URL` / `VITE_OAUTH_GOOGLE_URL` 로 전체 URL 을 지정합니다.
 *
 * @param {'kakao' | 'google'} provider
 * @returns {string}
 */
export function getOAuthLoginUrl(provider) {
  const override =
    provider === 'kakao'
      ? import.meta.env.VITE_OAUTH_KAKAO_URL
      : import.meta.env.VITE_OAUTH_GOOGLE_URL;
  if (override) {
    return override;
  }

  const pathTemplate = (
    import.meta.env.VITE_OAUTH_AUTHORIZATION_PATH ?? '/oauth2/authorization'
  ).replace(/\/$/, '');
  return `${pathTemplate}/${provider}`;
}
