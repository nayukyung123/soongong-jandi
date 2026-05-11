<template>
  <div class="flex h-screen w-full bg-app-canvas overflow-hidden font-sans text-app-ink">
    <Sidebar :active-page="currentPage" @change-page="uiStore.setPage" />

    <main class="flex-1 min-w-0 h-full overflow-x-hidden overflow-y-visible flex flex-col">
      <StudySession
        v-if="isStudying"
        v-model:all-plans="plansStore.allPlans"
        :current-date="plansStore.selectedDate"
        :start-task-index="studyStartIndex"
        @session-end="sessionStore.endStudy"
      />

      <CalendarView
        v-if="currentPage === 'calendar' && !isStudying"
        v-model:all-plans="plansStore.allPlans"
        v-model:selected-date="plansStore.selectedDate"
        @open-planner="uiStore.openPlannerModal"
        @request-plan-compose="uiStore.openPlanCompose"
      />

      <div
        v-if="currentPage !== 'calendar' && !isStudying"
        class="flex-1 flex items-center justify-center"
      >
        <p class="text-gray-300 font-black text-lg">🚧 준비 중입니다</p>
      </div>
    </main>

    <div
      v-if="showEndModal"
      class="fixed inset-0 z-[10200] flex items-center justify-center bg-black/45 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="session-end-modal-title"
    >
      <div
        class="max-h-[min(92vh,860px)] w-full max-w-lg overflow-y-auto rounded-3xl bg-white p-6 shadow-2xl sm:p-8"
        @click.stop
      >
        <div class="mb-6 text-center">
          <div class="mb-3 text-4xl">⏱️</div>
          <h3 id="session-end-modal-title" class="text-2xl font-black text-gray-900">공부를 마쳤어요</h3>
          <p class="mt-2 text-sm font-bold text-app-muted">
            정지한 세션 요약이에요. 아래에 오늘 공부 내용을 적어 두세요. 포도알(🍇)은 기록을 확인한 뒤, 앞으로 공부한 정도에 맞춰 드릴 예정이에요.
          </p>
          <div class="mt-4 flex flex-wrap justify-center gap-8 rounded-2xl bg-brand-50 p-4 sm:gap-12">
            <div class="text-center">
              <p class="text-xs font-bold text-gray-400">실 공부</p>
              <p class="text-xl font-black text-gray-800">{{ formatSecondsShort(sessionResult.elapsed) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs font-bold text-gray-400">자리비움</p>
              <p class="text-xl font-black text-gray-800">{{ formatSecondsShort(sessionResult.absenceTime) }}</p>
            </div>
          </div>
        </div>

        <label for="session-memo-input" class="mb-2 block text-sm font-black text-gray-800">공부한 내용</label>
        <textarea
          id="session-memo-input"
          v-model="sessionMemo"
          rows="6"
          placeholder="예: 챕터 2까지 풀었고, 중간에 자리 비웠다가 다시 돌아와 마무리했어요."
          class="mb-5 w-full resize-none rounded-2xl border-2 border-gray-100 bg-gray-50 p-4 text-sm font-bold text-gray-800 outline-none transition focus:border-brand-200"
        />

        <p class="mb-2 text-sm font-black text-gray-600">오늘 계획 중 완료한 항목을 표시할까요?</p>
        <div class="mb-6 max-h-40 space-y-2 overflow-y-auto">
          <label
            v-for="plan in todayPlans"
            :key="plan.id"
            class="flex cursor-pointer items-center gap-3 rounded-2xl border-2 p-3 transition hover:border-brand-200"
            :class="plan.completed ? 'border-brand-200 bg-brand-50' : 'border-gray-100'"
          >
            <input type="checkbox" v-model="plan.completed" class="h-5 w-5 accent-brand-600" />
            <span class="text-sm font-bold" :class="plan.completed ? 'text-gray-400 line-through' : 'text-gray-700'">
              {{ plan.title }}
            </span>
          </label>
        </div>

        <div class="flex gap-3">
          <button
            type="button"
            @click="sessionStore.discardSession"
            class="flex-1 py-3 border-2 border-gray-200 rounded-2xl font-black text-sm hover:bg-gray-50 transition"
          >
            버리기
          </button>
          <button
            type="button"
            @click="sessionStore.saveSession"
            class="flex-[2] px-8 py-3 bg-brand-600 text-white rounded-2xl font-black text-sm hover:bg-brand-700 transition shadow-lg"
          >
            ✓ 기록 저장
          </button>
        </div>
      </div>
    </div>

    <PlannerModal
      :show="plannerModalOpen && !isStudying"
      v-model:all-plans="plansStore.allPlans"
      v-model:current-date="plansStore.selectedDate"
      @close="uiStore.closePlannerModal"
    />

    <PlanComposeModal
      :show="planComposeOpen && !isStudying"
      :all-plans="plansStore.allPlans"
      :current-date="plansStore.selectedDate"
      @update:show="onPlanComposeShow"
      @update:all-plans="(next) => (plansStore.allPlans = next)"
    />

    <PlanTimerDetailModal
      :show="planTimerDetailOpen && !isStudying"
      @update:show="onPlanTimerDetailShow"
    />

    <FloatingPlanTimer v-if="planTimerVisible && !isStudying" />

    <CursorBeeFollower v-if="!isStudying && !showEndModal" />
  </div>
</template>

<script setup>
import { computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import Sidebar from './components/Sidebar.vue';
import PlannerModal from './components/PlannerModal.vue';
import PlanComposeModal from './components/PlanComposeModal.vue';
import PlanTimerDetailModal from './components/PlanTimerDetailModal.vue';
import FloatingPlanTimer from './components/FloatingPlanTimer.vue';
import CursorBeeFollower from './components/CursorBeeFollower.vue';
import CalendarView from './views/CalendarView.vue';
import StudySession from './views/StudySession.vue';
import { usePlansStore } from './stores/plans';
import { useUiStore } from './stores/ui';
import { useSessionStore } from './stores/session';
import { usePlanTimerStore } from './stores/planTimer';
import { formatSecondsShort } from './utils/formatSession';

const plansStore = usePlansStore();
const uiStore = useUiStore();
const sessionStore = useSessionStore();
const planTimerStore = usePlanTimerStore();

const { currentPage, plannerModalOpen, planComposeOpen, planTimerDetailOpen } = storeToRefs(uiStore);

function onPlanComposeShow(open) {
  if (!open) uiStore.closePlanCompose();
}

function onPlanTimerDetailShow(open) {
  if (!open) uiStore.closePlanTimerDetail();
}

const { isStudying, studyStartIndex, showEndModal, sessionResult, sessionMemo } = storeToRefs(sessionStore);
const { status: planTimerStatus } = storeToRefs(planTimerStore);

watch(planTimerStatus, (s) => {
  if (s === 'idle') uiStore.closePlanTimerDetail();
});

const planTimerVisible = computed(() => planTimerStatus.value !== 'idle');

const todayPlans = computed(() => plansStore.allPlans[plansStore.dateKey] || []);
</script>
