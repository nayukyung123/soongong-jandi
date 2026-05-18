import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useSessionStore } from './session';

export const useUiStore = defineStore('ui', () => {
  /** 메인 홈: 공부통계(stats) */
  const currentPage = ref('stats');
  const plannerModalOpen = ref(false);
  /** 플래너 모달 오픈 시 해당 plan 행을 바로 편집 모드로 (일간 「수정」) */
  const plannerFocusPlanId = ref(null);
  /** 새 계획 추가만 하는 소형 모달 */
  const planComposeOpen = ref(false);
  /** 플로팅 타이머 클릭 시: 현재 계획 상세 */
  const planTimerDetailOpen = ref(false);

  function setPage(page) {
    const session = useSessionStore();
    if (!session.isStudying) currentPage.value = page;
  }

  function goHome() {
    setPage('stats');
  }

  function goCalendar() {
    setPage('calendar');
  }

  function openPlannerModal(planId = null) {
    const session = useSessionStore();
    if (!session.isStudying) {
      plannerFocusPlanId.value = planId ?? null;
      plannerModalOpen.value = true;
    }
  }

  function closePlannerModal() {
    plannerModalOpen.value = false;
    planComposeOpen.value = false;
    plannerFocusPlanId.value = null;
  }

  function openPlanCompose() {
    const session = useSessionStore();
    if (session.isStudying) return;
    planComposeOpen.value = true;
  }

  function closePlanCompose() {
    planComposeOpen.value = false;
  }

  function openPlanTimerDetail() {
    const session = useSessionStore();
    if (session.isStudying) return;
    planTimerDetailOpen.value = true;
  }

  function closePlanTimerDetail() {
    planTimerDetailOpen.value = false;
  }

  return {
    currentPage,
    plannerModalOpen,
    plannerFocusPlanId,
    planComposeOpen,
    planTimerDetailOpen,
    setPage,
    goHome,
    goCalendar,
    openPlannerModal,
    closePlannerModal,
    openPlanCompose,
    closePlanCompose,
    openPlanTimerDetail,
    closePlanTimerDetail
  };
});
