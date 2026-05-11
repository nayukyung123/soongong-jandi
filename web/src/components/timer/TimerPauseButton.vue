<template>
  <button
    type="button"
    class="rounded-2xl border-2 border-gray-200 bg-white px-5 py-3 text-sm font-black text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
    :disabled="status !== 'running'"
    @click="planTimer.pause()"
  >
    일시정지
  </button>
</template>

<script setup>
import { storeToRefs } from 'pinia';
import { usePlanTimerStore } from '../../stores/planTimer.js';

const planTimer = usePlanTimerStore();
const { status } = storeToRefs(planTimer);
</script>
