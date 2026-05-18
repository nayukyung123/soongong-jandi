<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="calendar-main-modal-backdrop fixed inset-0 z-40 flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="titleId"
      @click.self="emit('close')"
    >
      <div
        class="relative flex h-[min(88vh,900px)] min-h-[min(72vh,760px)] max-h-[92vh] w-full max-w-7xl flex-col overflow-hidden rounded-3xl bg-app-canvas shadow-2xl ring-1 ring-black/5"
      >
        <header
          class="flex shrink-0 flex-wrap items-center justify-between gap-3 border-b border-gray-100 bg-white px-5 py-4"
        >
          <div class="min-w-0 flex-1 pr-2">
            <h2 :id="titleId" class="text-xl font-black tracking-tight text-gray-800 md:text-2xl">
              {{ headingTitle }}
            </h2>
            <p class="mt-1 text-xs font-bold text-app-muted">
              목록은 조회만 가능해요. 각 행에서 「수정」으로 고치거나,「계획 추가」로 새 항목만 적는 작은 창을 열 수 있어요.
            </p>
          </div>
          <div class="flex shrink-0 flex-wrap items-center gap-2">
            <button
              type="button"
              class="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm font-bold text-gray-700 hover:bg-gray-50"
              @click="shiftDay(-1)"
            >
              〈 어제
            </button>
            <button
              type="button"
              class="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm font-bold text-gray-700 hover:bg-gray-50"
              @click="shiftDay(1)"
            >
              내일 〉
            </button>
            <button
              type="button"
              class="rounded-2xl px-4 py-2 text-sm font-bold text-gray-500 hover:bg-gray-100"
              @click="emit('close')"
            >
              닫기
            </button>
          </div>
        </header>

        <div
          class="flex min-h-0 flex-1 flex-col gap-3 overflow-hidden px-4 pb-4 pt-2 md:px-6 md:pb-6"
        >
          <DayPlanner
            v-model:all-plans="allPlansModel"
            :current-date="currentDate"
            list-only
            list-plan-read-only
            class="min-h-0 flex-1"
            :focus-edit-plan-id="plannerFocusPlanId"
            @plan-timer-started="emit('close')"
            @before-start="emit('before-start', $event)"
          />
          <button
            type="button"
            class="shrink-0 rounded-2xl bg-brand-600 px-4 py-3.5 text-sm font-black text-white shadow-md transition hover:bg-brand-700"
            @click="uiStore.openPlanCompose()"
          >
            계획 추가
          </button>
        </div>
      </div>
    </div>

  </Teleport>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import DayPlanner from './DayPlanner.vue';
import { useUiStore } from '../stores/ui.js';

const props = defineProps({
  show: { type: Boolean, default: false },
  allPlans: { type: Object, required: true },
  currentDate: { type: Date, required: true }
});

const emit = defineEmits(['close', 'update:allPlans', 'update:currentDate', 'before-start']);

const uiStore = useUiStore();
const { plannerFocusPlanId } = storeToRefs(uiStore);

const titleId = 'planner-modal-heading';

/** 예: 5월 8일 (금)의 계획 — 선택된 날짜 기준 */
const headingTitle = computed(() => {
  const d = props.currentDate;
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  return `${d.getMonth() + 1}월 ${d.getDate()}일 (${days[d.getDay()]})의 계획`;
});

const allPlansModel = computed({
  get: () => props.allPlans,
  set: (v) => emit('update:allPlans', v)
});

function shiftDay(delta) {
  const d = new Date(props.currentDate);
  d.setDate(d.getDate() + delta);
  emit('update:currentDate', d);
}
</script>
