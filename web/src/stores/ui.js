import { defineStore } from 'pinia';
import { ref } from 'vue';
import { useSessionStore } from './session';

export const useUiStore = defineStore('ui', () => {
  /** 메인은 달력(calendar) */
  const currentPage = ref('calendar');
  const plannerModalOpen = ref(false);

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
  }

  return { currentPage, plannerModalOpen, setPage, openPlannerModal, closePlannerModal };
});
