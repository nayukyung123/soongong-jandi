<template>
  <div
    class="pointer-events-auto fixed bottom-5 right-5 z-[80] flex max-w-[min(100vw-1.5rem,420px)] flex-col gap-3 rounded-2xl border border-gray-200 bg-white/95 p-3 shadow-xl shadow-black/15 backdrop-blur-sm sm:bottom-6 sm:right-6 sm:flex-row sm:items-center sm:gap-4 sm:p-4"
    role="region"
    aria-label="계획 실행 타이머"
  >
    <button
      type="button"
      class="min-w-0 flex-1 rounded-xl px-1 py-0.5 text-left outline-none transition hover:bg-gray-50/95 focus-visible:ring-2 focus-visible:ring-brand-400"
      aria-label="계획 상세 보기"
      @click="uiStore.openPlanTimerDetail()"
    >
      <p class="truncate text-xs font-black text-gray-800">{{ activePlanTitle }}</p>
      <TimerDisplay size="md" />
    </button>
    <div class="flex shrink-0 items-center justify-end gap-2 sm:gap-3">
      <button
        v-if="timerStatus === 'running'"
        type="button"
        class="timer-grape-btn"
        aria-label="일시정지"
        @click="planTimer.pause()"
      >
        <img src="/images/timer/pause.png" alt="" />
      </button>
      <button
        v-else-if="timerStatus === 'paused'"
        type="button"
        class="timer-grape-btn"
        aria-label="재개"
        @click="planTimer.startOrResume()"
      >
        <img src="/images/timer/start.png" alt="" />
      </button>
      <button
        type="button"
        class="timer-grape-btn"
        aria-label="정지"
        @click="onStopTimer"
      >
        <img src="/images/timer/stop.png" alt="" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useUiStore } from '../stores/ui.js';
import { usePlansStore } from '../stores/plans.js';
import { usePlanTimerStore } from '../stores/planTimer.js';
import { useSessionStore } from '../stores/session.js';
import { toDateKey } from '../composables/useCalendarPlanHelpers.js';
import TimerDisplay from './timer/TimerDisplay.vue';

const uiStore = useUiStore();
const plansStore = usePlansStore();
const planTimer = usePlanTimerStore();
const sessionStore = useSessionStore();

/** 계획 타이머 정지 → 플로팅과 동일하게 공부 기록 모달 */
function onStopTimer() {
  const r = planTimer.stop();
  if (r == null) return;
  sessionStore.endStudy({
    elapsed: r.elapsedSeconds,
    accumulated: r.elapsedSeconds,
    absenceTime: 0,
    absenceCount: 0
  });
}
const { selectedPlanId, sessionDateKey, status: timerStatus } = storeToRefs(planTimer);

const activePlanTitle = computed(() => {
  const id = selectedPlanId.value;
  if (id == null) return '';
  const key = sessionDateKey.value ?? toDateKey(new Date());
  const list = plansStore.allPlans[key] || [];
  const plan = list.find((p) => p.id === id);
  return plan?.title?.trim() || '계획';
});
</script>
