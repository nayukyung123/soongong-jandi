<template>
  <button
    type="button"
    class="rounded-2xl bg-brand-600 px-5 py-3 text-sm font-black text-white shadow-md transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:opacity-40"
    :disabled="disabled"
    @click="planTimer.startOrResume()"
  >
    {{ label }}
  </button>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { usePlanTimerStore } from '../../stores/planTimer.js';

const planTimer = usePlanTimerStore();
const { status } = storeToRefs(planTimer);

const disabled = computed(() => !planTimer.canStartOrResume());

const label = computed(() => (status.value === 'paused' ? '재개' : '시작'));
</script>
