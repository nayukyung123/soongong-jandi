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
   * 완전 종료: 플랜에 실행 시간(초) 반영 후 idle.
   * 반환값의 undo로 공부 기록 모달에서 되돌아가기(일시정지·시간 복원) 가능.
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
    const planBefore = id != null ? list.find((p) => String(p.id) === String(id)) : null;

    /** @type {null | { type: 'planTimer', dateKey: string, planId: string | number, executedSeconds: number, completed: boolean, accumulatedMs: number, selectedPlanId: string | number, sessionDateKey: string }} */
    let undo = null;
    if (planBefore) {
      undo = {
        type: 'planTimer',
        dateKey: key,
        planId: id,
        executedSeconds: typeof planBefore.executedSeconds === 'number' ? planBefore.executedSeconds : 0,
        completed: !!planBefore.completed,
        accumulatedMs: totalMs,
        selectedPlanId: id,
        sessionDateKey: key,
      };
    }

    const next = list.map((p) => {
      if (String(p.id) !== String(id)) return p;
      const prev = typeof p.executedSeconds === 'number' ? p.executedSeconds : 0;
      return { ...p, executedSeconds: prev + seconds, completed: true };
    });
    plans.allPlans = { ...plans.allPlans, [key]: next };

    status.value = 'idle';
    accumulatedMs.value = 0;
    selectedPlanId.value = null;
    sessionDateKey.value = null;
    stopTick();
    tick.value += 1;
    return { elapsedSeconds: seconds, undo };
  }

  /** 공부 기록 모달에서 되돌아가기 — 플랜 되돌린 뒤 타이머를 곧바로 재개(running) */
  function restoreAfterStopUndo(undo) {
    if (!undo || undo.type !== 'planTimer') return false;
    const plans = usePlansStore();
    const key = undo.dateKey;
    const list = plans.allPlans[key];
    if (!Array.isArray(list)) return false;
    const idx = list.findIndex((p) => String(p.id) === String(undo.planId));
    if (idx === -1) return false;
    const p = list[idx];
    const next = [...list];
    next[idx] = {
      ...p,
      executedSeconds: undo.executedSeconds,
      completed: undo.completed,
    };
    plans.allPlans = { ...plans.allPlans, [key]: next };

    selectedPlanId.value = undo.selectedPlanId;
    sessionDateKey.value = undo.sessionDateKey;
    accumulatedMs.value = undo.accumulatedMs;
    segmentStartedAt.value = Date.now();
    status.value = 'running';
    startTick();
    tick.value += 1;
    return true;
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
    restoreAfterStopUndo,
    selectPlan,
    canStartOrResume,
  };
});
