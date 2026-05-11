/**
 * 공부 기록 제출 시 부여하는 포도알 등급.
 * 백엔드·분석 API 연동 후에는 assignGrapeReward 내부만 서버 호출로 교체하면 됩니다.
 */

export const GRAPE_TIERS = {
  insufficient: {
    id: 'insufficient',
    title: '미흡한 하루',
    subtitle: '오늘은 조금 아쉬웠어요. 다음엔 조금만 더 집중해 볼까요?',
    image: '/images/grapes/grape-insufficient.png'
  },
  best: {
    id: 'best',
    title: '최고로 열심히 했어요!',
    subtitle: '이 포도알은 오늘 가장 빛나는 공부에 드려요.',
    image: '/images/grapes/grape-best.png'
  },
  average: {
    id: 'average',
    title: '무난하게 잘했어요',
    subtitle: '보통 수준으로 꾸준히 해 냈어요.',
    image: '/images/grapes/grape-average.png'
  }
};

/**
 * @param {{ memo?: string, elapsed?: number }} input — 메모·공부 시간 등 (향후 분석용)
 * @returns {{ tierKey: keyof GRAPE_TIERS } & (typeof GRAPE_TIERS)[keyof GRAPE_TIERS]}
 */
/** 기록에 저장된 등급 키 → 포도알 이미지 경로 */
export function grapeImageForTier(tierKey) {
  const k = tierKey != null && GRAPE_TIERS[tierKey] ? tierKey : 'average';
  return GRAPE_TIERS[k].image;
}

export function assignGrapeReward(input = {}) {
  void input.memo;
  void input.elapsed;
  const keys = /** @type {(keyof typeof GRAPE_TIERS)[]} */ (
    Object.keys(GRAPE_TIERS)
  );
  const tierKey = keys[Math.floor(Math.random() * keys.length)];
  return { tierKey, ...GRAPE_TIERS[tierKey] };
}
