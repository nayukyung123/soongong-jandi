/** 플랜 객체(allPlans: Record<YYYY-MM-DD, Plan[]>)와 날짜 타일 상태 공통 처리 */

export function toDateKeyFromParts(year, month1Based, date) {
  return `${year}-${String(month1Based).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
}

export function toDateKey(date) {
  return toDateKeyFromParts(date.getFullYear(), date.getMonth() + 1, date.getDate());
}

export function startOfDay(date) {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
}

/** 일요일 시작 (달력 그리드와 동일) */
export function startOfWeekSunday(date) {
  const d = startOfDay(date);
  const day = d.getDay();
  d.setDate(d.getDate() - day);
  return d;
}

export function getPlansForDate(allPlans, date) {
  return allPlans[toDateKey(date)] || [];
}

/**
 * soil | grass | complete — CalendarBoard와 동일 규칙
 */
export function tileVariantForDate(allPlans, cellDate) {
  const plans = getPlansForDate(allPlans, cellDate);
  const hasPlans = plans.length > 0;
  const cellStart = startOfDay(cellDate);
  const todayStart = new Date();
  todayStart.setHours(0, 0, 0, 0);
  const isFutureDay = cellStart > todayStart;

  if (!hasPlans || isFutureDay) return 'soil';

  const allCompleted = plans.every((p) => Boolean(p.completed));
  if (allCompleted) return 'complete';

  return 'grass';
}
