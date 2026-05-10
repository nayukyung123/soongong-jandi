<template>
  <div class="max-h-[inherit] min-h-0 overflow-hidden bg-app-canvas p-6 md:p-8">
    <div
      class="flex min-h-0 flex-col overflow-hidden rounded-3xl border border-gray-100 bg-white p-6 shadow-sm md:p-8"
    >
      <h3 class="mb-5 shrink-0 text-lg font-black">📝 오늘의 계획</h3>

      <div class="mb-4 min-h-[120px] flex-1 space-y-3 overflow-y-auto pr-1">
        <div
          v-for="(plan, i) in currentPlans"
          :key="plan.id"
          class="group flex items-center gap-4 rounded-2xl border-2 border-gray-50 p-4 transition hover:border-brand-100"
        >
          <span class="cursor-grab text-lg text-gray-300">≡</span>
          <input type="checkbox" v-model="plan.completed" class="h-5 w-5 shrink-0 cursor-pointer accent-brand-600" />
          <span
            class="flex-1 text-sm font-bold"
            :class="plan.completed ? 'text-gray-300 line-through' : 'text-gray-700'"
          >
            {{ plan.title }}
          </span>
          <div class="flex shrink-0 items-center gap-1 rounded-xl bg-brand-50 px-3 py-1">
            <input
              type="number"
              v-model.number="plan.minutes"
              min="0"
              max="480"
              step="10"
              class="w-12 bg-transparent text-center text-sm font-black text-brand-600 outline-none"
              @click.stop
            />
            <span class="text-xs font-bold text-brand-400">분</span>
          </div>
          <button
            type="button"
            class="text-sm text-gray-300 opacity-0 transition hover:text-red-400 group-hover:opacity-100"
            @click="removePlan(i)"
          >
            ✕
          </button>
        </div>
      </div>

      <div v-if="currentPlans.length === 0" class="flex min-h-[100px] flex-1 items-center justify-center">
        <div class="w-full rounded-2xl border-2 border-dashed border-gray-100 p-10 text-center">
          <p class="text-sm font-bold text-gray-400">계획을 추가해보세요!</p>
        </div>
      </div>

      <div class="shrink-0 border-t border-gray-100 pt-4">
        <div
          class="flex gap-3 rounded-2xl border-2 border-transparent bg-gray-50 px-4 py-3 transition focus-within:border-brand-200"
        >
          <input
            v-model="newTitle"
            type="text"
            placeholder="+ 새 계획 추가..."
            class="flex-1 bg-transparent text-sm font-bold outline-none placeholder:text-gray-300"
            @keyup.enter="addPlan"
          />
          <button
            type="button"
            class="shrink-0 px-3 text-sm font-black text-brand-600 transition hover:scale-105"
            @click="addPlan"
          >
            추가
          </button>
        </div>
        <div class="mt-3 flex items-center justify-between px-1">
          <span class="text-xs font-bold text-gray-400">
            예상 시간: {{ totalMinutes }}분 · 총 {{ currentPlans.length }}개
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps(['allPlans', 'currentDate']);
const emit = defineEmits(['update:allPlans']);

const newTitle = ref('');

const dateKey = computed(() => {
  const d = props.currentDate;
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
});

const currentPlans = computed({
  get: () => props.allPlans[dateKey.value] || [],
  set: (val) => emit('update:allPlans', { ...props.allPlans, [dateKey.value]: val })
});

const addPlan = () => {
  if (!newTitle.value.trim()) return;
  currentPlans.value = [
    ...currentPlans.value,
    { id: Date.now(), title: newTitle.value, completed: false, minutes: 60 }
  ];
  newTitle.value = '';
};

const removePlan = (i) => {
  const copy = [...currentPlans.value];
  copy.splice(i, 1);
  currentPlans.value = copy;
};

const totalMinutes = computed(() => currentPlans.value.reduce((s, p) => s + (p.minutes || 0), 0));
</script>
