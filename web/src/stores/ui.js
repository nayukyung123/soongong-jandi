import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useSessionStore } from './session';

export const useUiStore = defineStore('ui', () => {
  /** 메인은 달력(calendar) */
  const currentPage = ref('calendar');
  const plannerModalOpen = ref(false);
  /** 새 계획 추가만 하는 소형 모달 */
  const planComposeOpen = ref(false);
  /** 플로팅 타이머 클릭 시: 현재 계획 상세 */
  const planTimerDetailOpen = ref(false);

  function setPage(page) {
    const session = useSessionStore();
    if (!session.isStudying) currentPage.value = page;
  }

  function openPlannerModal() {
    const session = useSessionStore();
    if (!session.isStudying) plannerModalOpen.value = true;
  }

  function closePlannerModal() {
    plannerModalOpen.value = false;
    planComposeOpen.value = false;
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
    planComposeOpen,
    planTimerDetailOpen,
    setPage,
    openPlannerModal,
    closePlannerModal,
    openPlanCompose,
    closePlanCompose,
    openPlanTimerDetail,
    closePlanTimerDetail
  };
});
