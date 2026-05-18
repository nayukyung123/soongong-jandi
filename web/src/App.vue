<template>
  <div class="flex h-screen min-h-0 w-full overflow-visible bg-app-canvas font-sans text-app-ink">
    <main class="relative flex min-h-0 min-w-0 flex-1 flex-col overflow-auto">
      <StudySession
        v-if="isStudying"
        v-model:all-plans="plansStore.allPlans"
        :current-date="plansStore.selectedDate"
        :start-task-index="studyStartIndex"
        :camera-stream="cameraStream"
        @session-end="sessionStore.endStudy"
      />

      <GrapeFarmStats v-if="currentPage === 'stats' && !isStudying" />

      <CalendarView
        v-else-if="currentPage === 'calendar' && !isStudying"
        v-model:all-plans="plansStore.allPlans"
        v-model:selected-date="plansStore.selectedDate"
        @open-planner="() => uiStore.openPlannerModal()"
        @request-plan-compose="uiStore.openPlanCompose"
        @go-home="uiStore.goHome"
        @request-plan-edit="(planId) => uiStore.openPlannerModal(planId)"
      />

      <!-- 통계 등: 모달을 뷰포트가 아니라 main 영역 중앙에 맞출 때 사용 -->
      <div
        id="main-modal-teleport"
        class="pointer-events-none absolute inset-0 z-[10180]"
        aria-hidden="true"
      />
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
            정지한 세션 요약이에요. 아래에 오늘 공부 내용을 적어 두세요. 제출하면 공부 내용에 맞는 포도알을 드릴 예정이며, 지금은 분석 API 준비 전이라 무작위로 한 알을 드려요.
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

    <div
      v-if="showGrapeRewardModal && lastGrapeAward"
      class="fixed inset-0 z-[10210] flex items-center justify-center bg-black/45 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="grape-reward-title"
    >
      <div
        class="w-full max-w-md overflow-hidden rounded-3xl bg-white p-6 text-center shadow-2xl sm:p-8"
        @click.stop
      >
        <p class="text-[11px] font-black uppercase tracking-wide text-brand-600">포도알 획득</p>
        <h3 id="grape-reward-title" class="mt-2 text-xl font-black text-gray-900">
          {{ lastGrapeAward.title }}
        </h3>
        <p class="mt-2 text-sm font-bold leading-relaxed text-gray-600">
          {{ lastGrapeAward.subtitle }}
        </p>
        <div class="my-6 flex justify-center">
          <img
            :src="lastGrapeAward.image"
            alt=""
            class="h-36 w-36 object-contain drop-shadow-md sm:h-44 sm:w-44"
            width="176"
            height="176"
          />
        </div>
        <p class="mb-6 text-xs font-bold text-gray-400">
          이후에는 공부 내용 분석 결과에 따라 미흡·보통·최고 열정 중 하나로 드릴 예정이에요.
        </p>
        <button
          type="button"
          class="w-full rounded-2xl bg-brand-600 py-3.5 text-sm font-black text-white shadow-lg transition hover:bg-brand-700"
          @click="sessionStore.closeGrapeRewardModal"
        >
          확인
        </button>
      </div>
    </div>

    <PlannerModal
      :show="plannerModalOpen && !isStudying"
      v-model:all-plans="plansStore.allPlans"
      v-model:current-date="plansStore.selectedDate"
      @close="uiStore.closePlannerModal"
      @before-start="requestCameraAndStart($event)"
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

    <CursorBeeFollower v-if="!isStudying && !showEndModal && !showGrapeRewardModal" />



  <!-- feature/fe/camera_perpission -->
  <button
    type="button"
    class="fixed bottom-4 right-4 z-50 rounded-2xl bg-brand-600 px-4 py-2 text-sm font-black text-white shadow-lg"
    @click="showCameraModal = true"
  >
    📷 카메라 모달 테스트
  </button>



  <CameraPermissionModal
    :show="showCameraModal"
    @allow="onCameraAllow"
    @later="onCameraLater"
  />
  <!-- 여기까지가 추가된 파일 -->

  </div>
</template>

<script setup>
// ref 임시 추가
import { computed, watch, ref } from 'vue';
import { storeToRefs } from 'pinia';
import PlannerModal from './components/PlannerModal.vue';
import PlanComposeModal from './components/PlanComposeModal.vue';
import PlanTimerDetailModal from './components/PlanTimerDetailModal.vue';
import FloatingPlanTimer from './components/FloatingPlanTimer.vue';
import CursorBeeFollower from './components/CursorBeeFollower.vue';
import CalendarView from './views/CalendarView.vue';
import GrapeFarmStats from './views/GrapeFarmStats.vue';
import StudySession from './views/StudySession.vue';
import { usePlansStore } from './stores/plans';
import { useUiStore } from './stores/ui';
import { useSessionStore } from './stores/session';
import { usePlanTimerStore } from './stores/planTimer';
import { formatSecondsShort } from './utils/formatSession';

// 임시 CameraPermissionModal
import CameraPermissionModal from './components/CameraPermissionModal.vue';

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

const {
  isStudying,
  studyStartIndex,
  showEndModal,
  sessionResult,
  sessionMemo,
  showGrapeRewardModal,
  lastGrapeAward,
} = storeToRefs(sessionStore);
const { status: planTimerStatus } = storeToRefs(planTimerStore);

watch(planTimerStatus, (s) => {
  if (s === 'idle') uiStore.closePlanTimerDetail();
});

const planTimerVisible = computed(() => planTimerStatus.value !== 'idle');

// 임시 추가된 showCameraModal
const showCameraModal = ref(false);

const cameraStream = ref(null);

function onCameraAllow(stream) {
  cameraStream.value = stream;
  showCameraModal.value = false;
  // pendingTimerAction 추가
  pendingTimerAction.value?.();
  pendingTimerAction.value = null;
}

// 추가: 타이머 시작 전 카메라 권한 체크
const pendingTimerAction = ref(null);  // 권한 후 실행할 함수 저장

function requestCameraAndStart(action) {
  if (cameraStream.value) {
    // 이미 권한 있으면 바로 실행
    action();
  } else {
    // 권한 없으면 모달 띄우고 액션 저장
    pendingTimerAction.value = action;
    showCameraModal.value = true;
  }
}



function onCameraLater() {
  showCameraModal.value = false;
  // 나중에 → 카메라 없이 그냥 타이머 시작
  pendingTimerAction.value?.();
  pendingTimerAction.value = null;
}


</script>