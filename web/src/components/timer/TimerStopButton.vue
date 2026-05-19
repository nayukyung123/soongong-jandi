<template>
  <button
    type="button"
    class="rounded-2xl border-2 border-red-100 bg-red-50 px-5 py-3 text-sm font-black text-red-700 transition hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-40"
    :disabled="status === 'idle'"
    @click="onStopTimer"
  >
    정지
  </button>
</template>

<script setup>
import { storeToRefs } from 'pinia';
import { usePlanTimerStore } from '../../stores/planTimer.js';
import { useSessionStore } from '../../stores/session.js';

const planTimer = usePlanTimerStore();
const sessionStore = useSessionStore();
const { status } = storeToRefs(planTimer);

function onStopTimer() {
  const r = planTimer.stop();
  if (r == null) return;
  sessionStore.endStudy({
    elapsed: r.elapsedSeconds,
    accumulated: r.elapsedSeconds,
    absenceTime: 0,
    absenceCount: 0,
    undo: r.undo,
  });
}
</script>
