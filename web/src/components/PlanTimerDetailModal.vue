<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 z-[90] flex items-center justify-center bg-black/45 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="plan-timer-detail-title"
      @click.self="emitClose"
    >
      <div
        class="max-h-[85vh] w-full max-w-md overflow-y-auto overscroll-contain rounded-3xl bg-white p-6 shadow-2xl ring-1 ring-black/5"
        @click.stop
      >
        <header class="mb-5 flex items-start justify-between gap-3 border-b border-gray-100 pb-4">
          <div class="min-w-0 flex-1">
            <p class="text-[11px] font-black uppercase tracking-wide text-brand-600">진행 중인 계획</p>
            <h2 id="plan-timer-detail-title" class="mt-1 break-words text-xl font-black leading-snug text-gray-900">
              {{ activePlan?.title?.trim() || '계획' }}
            </h2>
          </div>
          <button
            type="button"
            class="shrink-0 rounded-2xl px-3 py-2 text-sm font-bold text-gray-500 hover:bg-gray-100"
            @click="emitClose"
          >
            닫기
          </button>
        </header>

        <div v-if="activePlan" class="space-y-4">
          <div class="rounded-2xl border border-brand-100 bg-brand-50/90 px-4 py-4">
            <p class="text-[10px] font-black uppercase tracking-wide text-brand-600">이번 세션 경과</p>
            <TimerDisplay size="lg" />
            <p class="mt-2 text-xs font-bold text-gray-600">{{ statusLabel }}</p>
          </div>

          <div v-if="activePlan.detail" class="rounded-2xl bg-gray-50 px-4 py-3 text-sm">
            <p class="text-[10px] font-black uppercase tracking-wide text-gray-400">계획 내용</p>
            <p class="mt-1 whitespace-pre-wrap font-bold leading-relaxed text-gray-800">{{ activePlan.detail }}</p>
          </div>
        </div>

        <p v-else class="rounded-2xl bg-gray-50 px-4 py-6 text-center text-sm font-bold text-gray-500">
          계획 정보를 불러올 수 없습니다.
        </p>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { usePlansStore } from '../stores/plans.js';
import { usePlanTimerStore } from '../stores/planTimer.js';
import { toDateKey } from '../composables/useCalendarPlanHelpers.js';
import TimerDisplay from './timer/TimerDisplay.vue';

defineProps({
  show: { type: Boolean, default: false }
});

const emit = defineEmits(['update:show']);

const plansStore = usePlansStore();
const planTimer = usePlanTimerStore();
const { selectedPlanId, sessionDateKey, status } = storeToRefs(planTimer);

const activePlan = computed(() => {
  const id = selectedPlanId.value;
  if (id == null) return null;
  const key = sessionDateKey.value ?? toDateKey(new Date());
  const list = plansStore.allPlans[key] || [];
  return list.find((p) => String(p.id) === String(id)) ?? null;
});

const statusLabel = computed(() => {
  if (status.value === 'running') return '측정 중입니다.';
  if (status.value === 'paused') return '일시정지 상태입니다.';
  return '';
});

function emitClose() {
  emit('update:show', false);
}
</script>
