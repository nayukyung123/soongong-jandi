<template>
  <div
    class="tabular-nums tracking-tight font-black text-gray-900"
    :class="sizeClass"
    role="timer"
    :aria-live="status === 'running' ? 'polite' : 'off'"
    :aria-label="`경과 시간 ${formatted}`"
  >
    {{ formatted }}
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { usePlanTimerStore } from '../../stores/planTimer.js';

const props = defineProps({
  /** 'lg' | 'md' */
  size: { type: String, default: 'lg' }
});

const planTimer = usePlanTimerStore();
const { elapsedMs, status } = storeToRefs(planTimer);

const sizeClass = computed(() =>
  props.size === 'md' ? 'text-2xl sm:text-3xl' : 'text-3xl sm:text-5xl'
);

const formatted = computed(() => {
  elapsedMs.value;
  const totalSec = Math.floor((elapsedMs.value ?? 0) / 1000);
  const h = Math.floor(totalSec / 3600);
  const m = Math.floor((totalSec % 3600) / 60);
  const s = totalSec % 60;
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});
</script>
