import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { usePlansStore } from './plans.js';
import { toDateKey } from '../composables/useCalendarPlanHelpers.js';

function todayDateKey() {
  return toDateKey(new Date());
}

export const usePlanTimerStore = defineStore('planTimer', () => {
  /** 'idle' | 'running' | 'paused' */
  const status = ref('idle');
  const selectedPlanId = ref(null);
  /** 시작 시점의 plans 날짜 키 — 정지 시 실행 시간 반영에 사용 */
  const sessionDateKey = ref(null);
  /** 누적 + 현재 구간 */
  const accumulatedMs = ref(0);
  const segmentStartedAt = ref(null);
  /** 화면 갱신용 틱 */
  const tick = ref(0);

  let intervalId = null;

  function stopTick() {
    if (intervalId != null) {
      clearInterval(intervalId);
      intervalId = null;
    }
  }

  function startTick() {
    stopTick();
    intervalId = setInterval(() => {
      tick.value += 1;
    }, 250);
  }

  const elapsedMs = computed(() => {
    tick.value;
    if (status.value === 'running' && segmentStartedAt.value != null) {
      return accumulatedMs.value + (Date.now() - segmentStartedAt.value);
    }
    return accumulatedMs.value;
  });

  const isViewingToday = computed(() => usePlansStore().dateKey === todayDateKey());

  function canStartOrResume(state = status.value) {
    return (
      isViewingToday.value &&
      selectedPlanId.value != null &&
      (state === 'idle' || state === 'paused')
    );
  }

  function startOrResume() {
    if (!canStartOrResume()) return false;
    if (status.value === 'idle') {
      accumulatedMs.value = 0;
      sessionDateKey.value = usePlansStore().dateKey;
    }
    segmentStartedAt.value = Date.now();
    status.value = 'running';
    startTick();
    return true;
  }

  /**
   * 목록의 「시작」에서 호출: 오늘 날짜의 계획만, 즉시 측정 시작.
   */
  function startPlanImmediate(planId) {
    const plans = usePlansStore();
    const key = plans.dateKey;
    const tk = todayDateKey();
    if (key !== tk) return false;
    if (status.value !== 'idle') return false;

    const list = plans.allPlans[key] || [];
    const target = list.find((p) => p.id === planId);
    if (!target || target.completed) return false;

    selectedPlanId.value = planId;
    sessionDateKey.value = key;
    accumulatedMs.value = 0;
    segmentStartedAt.value = Date.now();
    status.value = 'running';
    startTick();
    return true;
  }

  function pause() {
    if (status.value !== 'running' || segmentStartedAt.value == null) return false;
    accumulatedMs.value += Date.now() - segmentStartedAt.value;
    segmentStartedAt.value = null;
    status.value = 'paused';
    stopTick();
    tick.value += 1;
    return true;
  }

  /**
   * 완전 종료: 오늘 날짜의 선택된 플랜에 실행 시간(초)을 더합니다.
   */
  /**
   * 완전 종료. 성공 시 이번 세션 구간의 초를 반환하고, idle이면 null.
   * (호출 측에서 공부 기록 모달 등에 넘길 수 있음)
   */
  function stop() {
    if (status.value === 'idle') return null;

    if (status.value === 'running' && segmentStartedAt.value != null) {
      accumulatedMs.value += Date.now() - segmentStartedAt.value;
      segmentStartedAt.value = null;
    }

    const totalMs = accumulatedMs.value;
    const seconds = Math.max(0, Math.round(totalMs / 1000));

    const plans = usePlansStore();
    const key = sessionDateKey.value ?? plans.dateKey;
    const list = plans.allPlans[key] || [];
    const id = selectedPlanId.value;
    const next = list.map((p) => {
      if (p.id !== id) return p;
      const prev = typeof p.executedSeconds === 'number' ? p.executedSeconds : 0;
      return { ...p, executedSeconds: prev + seconds };
    });
    plans.allPlans = { ...plans.allPlans, [key]: next };

    status.value = 'idle';
    accumulatedMs.value = 0;
    selectedPlanId.value = null;
    sessionDateKey.value = null;
    stopTick();
    tick.value += 1;
    return { elapsedSeconds: seconds };
  }

  function selectPlan(planId) {
    if (status.value !== 'idle') return;
    selectedPlanId.value = planId;
  }

  return {
    status,
    selectedPlanId,
    sessionDateKey,
    elapsedMs,
    isViewingToday,
    tick,
    startOrResume,
    startPlanImmediate,
    pause,
    stop,
    selectPlan,
    canStartOrResume,
  };
});
