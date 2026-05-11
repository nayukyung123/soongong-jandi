<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="calendar-main-modal-backdrop fixed inset-y-0 right-0 left-56 z-[60] flex items-center justify-center bg-black/50 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      aria-labelledby="plan-compose-title"
      @click.self="close"
    >
      <div
        class="flex max-h-[min(88vh,560px)] w-full max-w-2xl flex-col overflow-hidden rounded-3xl bg-white shadow-2xl ring-1 ring-black/5"
        @click.stop
      >
        <header class="flex shrink-0 items-start justify-between gap-3 border-b border-gray-100 px-5 py-4">
          <div class="min-w-0">
            <h2 id="plan-compose-title" class="text-lg font-black tracking-tight text-gray-900 md:text-xl">
              새 계획 추가
            </h2>
            <p class="mt-1 text-xs font-bold text-gray-500">{{ modalSubtitle }}</p>
          </div>
          <button
            type="button"
            class="shrink-0 rounded-2xl px-3 py-2 text-sm font-bold text-gray-500 hover:bg-gray-100"
            @click="close"
          >
            닫기
          </button>
        </header>

        <div class="overflow-y-auto overscroll-contain px-4 pb-5 pt-3 md:px-6">
          <DayPlanner
            compose-only
            :all-plans="allPlans"
            :current-date="currentDate"
            @update:all-plans="emit('update:allPlans', $event)"
            @plan-added="close"
          />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue';
import DayPlanner from './DayPlanner.vue';

const props = defineProps({
  show: { type: Boolean, default: false },
  allPlans: { type: Object, required: true },
  currentDate: { type: Date, required: true }
});

const emit = defineEmits(['update:show', 'update:allPlans']);

const dateSubtitle = computed(() => {
  const d = props.currentDate;
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 (${days[d.getDay()]})`;
});

const modalSubtitle = computed(
  () => `${dateSubtitle.value} · 이 창에서는 새 항목만 추가할 수 있어요. 목록은 계획 모달에서 확인하세요.`
);

function close() {
  emit('update:show', false);
}
</script>
