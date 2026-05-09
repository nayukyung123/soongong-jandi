<template>
  <div class="flex h-screen w-full bg-[#F8F9FA] overflow-hidden font-sans text-[#343A40]">
    <Sidebar :active-page="currentPage" @change-page="setPage" />

    <main class="flex-1 min-w-0 h-full overflow-hidden flex flex-col">

      <PlannerView
        v-if="currentPage === 'planner' && !isStudying"
        v-model:all-plans="allPlans"
        v-model:current-date="selectedDate"
        @start-study="startStudy"
      />

      <!-- 공부 중 화면 -->
      <StudySession
        v-if="isStudying"
        v-model:all-plans="allPlans"
        :current-date="selectedDate"
        :start-task-index="studyStartIndex"
        @session-end="endStudy"
      />

      <CalendarView
        v-if="currentPage === 'calendar' && !isStudying"
        v-model:all-plans="allPlans"
        v-model:selected-date="selectedDate"
        @change-page="setPage"
      />

      <div v-if="!['planner','calendar'].includes(currentPage) && !isStudying"
           class="flex-1 flex items-center justify-center">
        <p class="text-gray-300 font-black text-lg">🚧 준비 중입니다</p>
      </div>
    </main>

    <!-- 세션 종료 모달 -->
    <div v-if="showEndModal" class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">
      <div class="bg-white rounded-3xl p-8 w-[480px] shadow-2xl">
        <div class="text-center mb-6">
          <div class="text-4xl mb-3">⏱️</div>
          <h3 class="text-2xl font-black">공부 기록 작성</h3>
          <div class="flex justify-center gap-6 mt-4 bg-purple-50 rounded-2xl p-4">
            <div class="text-center">
              <p class="text-xs text-gray-400 font-bold">실 공부</p>
              <p class="text-xl font-black text-gray-800">{{ formatMin(sessionResult.elapsed) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-gray-400 font-bold">자리비움</p>
              <p class="text-xl font-black text-gray-800">{{ formatMin(sessionResult.absenceTime) }}</p>
            </div>
            <div class="text-center">
              <p class="text-xs text-gray-400 font-bold">얻은 알</p>
              <p class="text-xl font-black text-gray-800">+{{ sessionResult.completedGrapes }} 🍇</p>
            </div>
          </div>
        </div>

        <!-- 계획 체크 -->
        <p class="text-sm font-black text-gray-600 mb-3">오늘 계획 중 완료한 것은?</p>
        <div class="space-y-2 mb-5 max-h-40 overflow-y-auto">
          <label
            v-for="plan in todayPlans"
            :key="plan.id"
            class="flex items-center gap-3 p-3 border-2 rounded-2xl cursor-pointer hover:border-purple-200 transition"
            :class="plan.completed ? 'border-purple-200 bg-purple-50' : 'border-gray-100'"
          >
            <input type="checkbox" v-model="plan.completed" class="w-5 h-5 accent-purple-600" />
            <span class="text-sm font-bold" :class="plan.completed ? 'line-through text-gray-400' : 'text-gray-700'">
              {{ plan.title }}
            </span>
          </label>
        </div>

        <!-- 한줄 메모 -->
        <textarea
          v-model="sessionMemo"
          placeholder="예: chap 2까지 풀고 자리 비렸다가 좀 돌아옴..."
          class="w-full bg-gray-50 border-2 border-gray-100 rounded-2xl p-4 text-sm font-bold resize-none outline-none focus:border-purple-200 transition h-20 mb-5"
        />

        <div class="flex gap-3">
          <button
            @click="discardSession"
            class="flex-1 py-3 border-2 border-gray-200 rounded-2xl font-black text-sm hover:bg-gray-50 transition"
          >
            버리기
          </button>
          <button
            @click="saveSession"
            class="flex-2 px-8 py-3 bg-purple-600 text-white rounded-2xl font-black text-sm hover:bg-purple-700 transition shadow-lg"
          >
            ✓ 기록 저장
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import Sidebar from './components/Sidebar.vue';
import PlannerView from './views/PlannerView.vue';
import CalendarView from './views/CalendarView.vue';
import StudySession from './views/StudySession.vue';

const currentPage = ref('planner');
const selectedDate = ref(new Date());
const allPlans = ref(JSON.parse(localStorage.getItem('total_study_data') || '{}'));

watch(allPlans, (val) => {
  localStorage.setItem('total_study_data', JSON.stringify(val));
}, { deep: true });

const setPage = (page) => {
  if (!isStudying.value) currentPage.value = page;
};

// ─── 공부 세션 상태 ────────────────────────────────
const isStudying = ref(false);
const studyStartIndex = ref(0);
const showEndModal = ref(false);
const sessionResult = ref({});
const sessionMemo = ref('');

const dateKey = computed(() => {
  const d = selectedDate.value;
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
});

const todayPlans = computed(() => allPlans.value[dateKey.value] || []);

// PlannerView에서 공부 시작 이벤트
const startStudy = (taskIndex = 0) => {
  studyStartIndex.value = taskIndex;
  isStudying.value = true;
  currentPage.value = 'planner'; // 사이드바 planner 유지
};

// StudySession에서 종료 이벤트
const endStudy = (result) => {
  sessionResult.value = result;
  isStudying.value = false;
  showEndModal.value = true;
};

const formatMin = (seconds) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m >= 60) return `${Math.floor(m/60)}h ${m%60}m`;
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
};

const saveSession = () => {
  // 메모나 세션 기록을 localStorage에 저장할 수 있음
  const records = JSON.parse(localStorage.getItem('study_records') || '[]');
  records.push({
    date: dateKey.value,
    elapsed: sessionResult.value.elapsed,
    absence: sessionResult.value.absenceTime,
    grapes: sessionResult.value.completedGrapes,
    memo: sessionMemo.value,
    savedAt: new Date().toISOString()
  });
  localStorage.setItem('study_records', JSON.stringify(records));
  showEndModal.value = false;
  sessionMemo.value = '';
};

const discardSession = () => {
  showEndModal.value = false;
  sessionMemo.value = '';
};
</script>