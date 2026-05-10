<template>
  <div
    :class="
      transparentVariant
        ? 'day-planner-root--embedded flex h-full max-h-full min-h-0 flex-col overflow-hidden bg-transparent p-3 sm:p-4 md:p-5'
        : 'day-planner-root--modal flex h-full min-h-0 flex-col overflow-hidden bg-app-canvas p-6 md:p-8'
    "
  >
    <div
      :class="
        transparentVariant
          ? 'day-planner-card--embedded flex min-h-0 flex-1 flex-col overflow-hidden rounded-3xl border border-white/45 bg-transparent p-4 md:p-6'
          : 'day-planner-card--modal flex h-full min-h-0 flex-col overflow-hidden rounded-3xl border border-gray-100 bg-white p-6 shadow-sm md:p-8'
      "
    >
      <h3
        :class="[
          'mb-4 shrink-0 text-lg font-black',
          transparentVariant ? embeddedPalette.heading : 'text-app-ink drop-shadow-sm'
        ]"
      >
        {{ headingText }}
      </h3>

      <!-- 일간 embedded -->
      <template v-if="transparentVariant">
        <div
          v-if="hasPlans"
          :class="[
            'mb-4 min-h-0 space-y-3 overflow-y-auto overscroll-contain pr-1 day-planner-scroll-area'
          ]"
        >
          <div
            v-for="(plan, i) in currentPlans"
            :key="plan.id"
            class="group flex items-center gap-4 rounded-2xl border-2 border-white/55 bg-transparent p-4 transition hover:border-brand-200/90"
          >
            <span class="text-lg" :class="embeddedPalette.grab">≡</span>
            <input type="checkbox" v-model="plan.completed" class="h-5 w-5 shrink-0 cursor-pointer accent-brand-600" />
            <span
              class="text-sm font-bold"
              :class="plan.completed ? embeddedPalette.planDone : embeddedPalette.planActive"
            >
              {{ plan.title }}
            </span>
            <div class="flex shrink-0 items-center gap-1 rounded-xl border border-white/50 bg-transparent px-3 py-1">
              <input
                type="number"
                v-model.number="plan.minutes"
                min="0"
                max="480"
                step="10"
                :class="embeddedPalette.minuteNum"
                @click.stop
              />
              <span class="text-xs font-bold" :class="embeddedPalette.minuteSuffix">분</span>
            </div>
            <button type="button" :class="embeddedPalette.deleteBtn" @click="removePlan(i)">
              ✕
            </button>
          </div>
        </div>

        <div v-else class="day-planner-empty-area mb-4 flex min-h-0 shrink-0 flex-col justify-center">
          <div
            class="day-planner-empty-area__inner flex w-full items-center justify-center rounded-2xl border-2 border-dashed border-white/55 bg-transparent py-10 text-center"
          >
            <p class="text-sm font-bold" :class="embeddedPalette.emptyHint">
              계획을 추가해보세요!
            </p>
          </div>
        </div>
      </template>

      <!-- 모달: 중간 영역 높이 고정 · 목록만 스크롤 · 입력은 하단 고정 -->
      <template v-else>
        <div class="day-planner-modal-middle mb-4 min-h-0 shrink-0">
          <div v-if="hasPlans" class="day-planner-modal-scroll space-y-3">
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
          <div v-else class="day-planner-modal-empty">
            <div
              class="flex h-full w-full items-center justify-center rounded-2xl border-2 border-dashed border-gray-100 bg-gray-50/60 px-6 py-10 text-center"
            >
              <p class="text-sm font-bold text-gray-400">계획을 추가해보세요!</p>
            </div>
          </div>
        </div>
      </template>

      <div
        :class="[
          'shrink-0 pt-4',
          transparentVariant ? 'border-t border-white/35' : 'mt-auto border-t border-gray-100'
        ]"
      >
        <div
          :class="[
            'flex gap-3 rounded-2xl border-2 px-4 py-3 transition',
            transparentVariant
              ? 'border-white/55 bg-transparent focus-within:border-brand-300/90 focus-within:ring-2 focus-within:ring-brand-200/30'
              : 'border-transparent bg-gray-50 focus-within:border-brand-200'
          ]"
        >
          <input
            v-model="newTitle"
            type="text"
            placeholder="+ 새 계획 추가..."
            :class="[
              'flex-1 bg-transparent text-sm font-bold outline-none',
              transparentVariant ? embeddedPalette.newInput : 'placeholder:text-gray-300'
            ]"
            @keyup.enter="addPlan"
          />
          <button
            type="button"
            :class="[
              'shrink-0 px-3 text-sm font-black transition hover:scale-105',
              transparentVariant ? embeddedPalette.addBtn : 'text-brand-600'
            ]"
            @click="addPlan"
          >
            추가
          </button>
        </div>
        <div class="mt-3 flex items-center justify-between px-1">
          <span
            :class="
              transparentVariant ? embeddedPalette.footerMeta : 'text-xs font-bold text-gray-400'
            "
          >
            예상 시간: {{ totalMinutes }}분 · 총 {{ currentPlans.length }}개
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  allPlans: { type: Object, required: true },
  currentDate: { type: Date, required: true },
  /** 일간 달력 등 배경 타일 위에 올릴 때 카드·바깥 배경 투명 처리 */
  transparentVariant: { type: Boolean, default: false },
  /** 일간 배경 타일 종류 — 글자 대비용 (soil | grass | complete) */
  embeddedSurface: {
    type: String,
    default: null,
    validator: (v) => v == null || ['soil', 'grass', 'complete'].includes(v)
  }
});

const emit = defineEmits(['update:allPlans']);

const headingText = computed(() => {
  const d = props.currentDate;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const cur = new Date(d);
  cur.setHours(0, 0, 0, 0);
  if (cur.getTime() === today.getTime()) return '📝 오늘의 계획';
  return '📝 이 날의 계획';
});

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

const hasPlans = computed(() => currentPlans.value.length > 0);

/** 일간: 흙밭은 밝은 글자+짙은 그림자, 잔디/완료는 진한 글자+밝은 후광 */
const embeddedPalette = computed(() => {
  const idle = {
    heading: '',
    planActive: '',
    planDone: '',
    emptyHint: '',
    footerMeta: '',
    newInput: '',
    grab: '',
    minuteNum: '',
    minuteSuffix: '',
    addBtn: '',
    deleteBtn: ''
  };
  if (!props.transparentVariant) return idle;

  const halo =
    '[text-shadow:0_1px_2px_rgba(255,255,255,0.98),0_0_12px_rgba(255,255,255,0.82),0_0_2px_rgba(255,255,255,0.95)]';
  const soilShadow =
    '[text-shadow:0_1px_4px_rgba(35,22,10,0.95),0_0_14px_rgba(0,0,0,0.55)]';
  const soilSoft =
    '[text-shadow:0_1px_3px_rgba(35,22,10,0.92),0_0_10px_rgba(0,0,0,0.42)]';

  const s = props.embeddedSurface ?? 'grass';

  if (s === 'soil') {
    return {
      heading: `text-[#fff9f0] ${soilShadow}`,
      planActive: `flex-1 text-[#fffdf8] ${soilSoft}`,
      planDone: 'flex-1 text-stone-400 line-through',
      emptyHint: `text-[#fff5e8] ${soilSoft}`,
      footerMeta: `text-xs font-bold text-[#ede8e0] ${soilSoft}`,
      newInput:
        'text-[#fffdf8] placeholder:text-stone-400/90 [text-shadow:0_1px_2px_rgba(35,22,10,0.85)]',
      grab: 'cursor-grab text-stone-400',
      minuteNum: `w-12 bg-transparent text-center text-sm font-black text-brand-200 outline-none ${soilSoft}`,
      minuteSuffix: 'text-brand-300',
      addBtn: `text-brand-200 ${soilSoft}`,
      deleteBtn:
        'text-sm text-stone-500 opacity-0 transition hover:text-red-300 group-hover:opacity-100 [text-shadow:0_1px_2px_rgba(0,0,0,0.65)]'
    };
  }

  /* grass · complete — 동일 계열 */
  return {
    heading: `text-gray-900 ${halo}`,
    planActive: `flex-1 text-gray-900 ${halo}`,
    planDone: 'flex-1 text-gray-600 line-through',
    emptyHint: `text-gray-900 ${halo}`,
    footerMeta: `text-xs font-bold text-gray-800 [text-shadow:0_1px_2px_rgba(255,255,255,0.9),0_0_10px_rgba(255,255,255,0.65)]`,
    newInput: 'text-gray-900 placeholder:text-gray-600',
    grab: 'cursor-grab text-gray-600',
    minuteNum: 'w-12 bg-transparent text-center text-sm font-black text-brand-700 outline-none',
    minuteSuffix: 'text-brand-500',
    addBtn: 'text-brand-600',
    deleteBtn:
      'text-sm text-gray-500 opacity-0 transition hover:text-red-500 group-hover:opacity-100'
  };
});
</script>

<style scoped>
/* 모달: 중간(목록/빈 상태) 박스 높이 동일, 목록만 스크롤 — 아래 입력란은 템플릿에서 shrink-0 */
.day-planner-modal-middle {
  display: flex;
  flex-direction: column;
  height: clamp(240px, 42vh, 360px);
  overflow: hidden;
  border-radius: 1rem;
  border: 1px solid rgb(243 244 246);
  background: rgb(249 250 251 / 0.65);
}

.day-planner-modal-scroll {
  box-sizing: border-box;
  flex: 1 1 0;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 1rem 0.75rem 1rem 1rem;
}

.day-planner-modal-empty {
  box-sizing: border-box;
  flex: 1 1 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 1rem;
}
</style>
