/** 초 단위 경과 시간을 짧은 문자열로 표시 */
export function formatSecondsShort(seconds) {
  const s = Number(seconds) || 0;
  const m = Math.floor(s / 60);
  const rem = s % 60;
  if (m >= 60) return `${Math.floor(m / 60)}h ${m % 60}m`;
  return m > 0 ? `${m}m ${rem}s` : `${rem}s`;
}
