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
      class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50"
    >
      <div class="bg-white rounded-3xl p-8 w-[480px] shadow-2xl">
        <div class="text-center mb-6">
          <div class="text-4xl mb-3">⏱️</div>
          <h3 class="text-2xl font-black">공부 기록 작성</h3>
          <div class="flex justify-center gap-6 mt-4 bg-brand-50 rounded-2xl p-4">
            <div class="text-center">
              <p class="text-xs text-gray-400 font-bold">실 공부</p>
              <p class="text-xl font-black text-gray-800">{{ formatSecondsShort(sessionResult.elapsed) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-gray-400 font-bold">자리비움</p>
              <p class="text-xl font-black text-gray-800">{{ formatSecondsShort(sessionResult.absenceTime) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-gray-400 font-bold">얻은 알</p>
              <p class="text-xl font-black text-gray-800">+{{ sessionResult.completedGrapes }} 🍇</p>
            </div>
          </div>
        </div>

        <p class="text-sm font-black text-gray-600 mb-3">오늘 계획 중 완료한 것은?</p>
        <div class="space-y-2 mb-5 max-h-40 overflow-y-auto">
          <label
            v-for="plan in todayPlans"
            :key="plan.id"
            class="flex items-center gap-3 p-3 border-2 rounded-2xl cursor-pointer hover:border-brand-200 transition"
            :class="plan.completed ? 'border-brand-200 bg-brand-50' : 'border-gray-100'"
          >
            <input type="checkbox" v-model="plan.completed" class="w-5 h-5 accent-brand-600" />
            <span class="text-sm font-bold" :class="plan.completed ? 'line-through text-gray-400' : 'text-gray-700'">
              {{ plan.title }}
            </span>
          </label>
        </div>

        <textarea
          v-model="sessionMemo"
          placeholder="예: chap 2까지 풀고 자리 비렸다가 좀 돌아옴..."
          class="w-full bg-gray-50 border-2 border-gray-100 rounded-2xl p-4 text-sm font-bold resize-none outline-none focus:border-brand-200 transition h-20 mb-5"
        />

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
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import Sidebar from './components/Sidebar.vue';
import PlannerModal from './components/PlannerModal.vue';
import CalendarView from './views/CalendarView.vue';
import StudySession from './views/StudySession.vue';
import { usePlansStore } from './stores/plans';
import { useUiStore } from './stores/ui';
import { useSessionStore } from './stores/session';
import { formatSecondsShort } from './utils/formatSession';

const plansStore = usePlansStore();
const uiStore = useUiStore();
const sessionStore = useSessionStore();

const { currentPage, plannerModalOpen } = storeToRefs(uiStore);
const { isStudying, studyStartIndex, showEndModal, sessionResult, sessionMemo } = storeToRefs(sessionStore);

const todayPlans = computed(() => plansStore.allPlans[plansStore.dateKey] || []);
</script>
