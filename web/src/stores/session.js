import { defineStore } from 'pinia';
import { ref } from 'vue';
import { usePlansStore } from './plans';

const RECORDS_KEY = 'study_records';

export const useSessionStore = defineStore('session', () => {
  const isStudying = ref(false);
  const studyStartIndex = ref(0);
  const showEndModal = ref(false);
  const sessionResult = ref({});
  const sessionMemo = ref('');

  function startStudy(taskIndex = 0) {
    studyStartIndex.value = taskIndex;
    isStudying.value = true;
  }

  function endStudy(result) {
    sessionResult.value = result;
    sessionMemo.value = '';
    isStudying.value = false;
    showEndModal.value = true;
  }

  function saveSession() {
    const plans = usePlansStore();
    const records = JSON.parse(localStorage.getItem(RECORDS_KEY) || '[]');
    records.push({
      date: plans.dateKey,
      elapsed: sessionResult.value.elapsed,
      absence: sessionResult.value.absenceTime,
      grapes: 0, // 추후 기록·성과 기반 지급 (UI에서 별도 안내)
      memo: sessionMemo.value,
      savedAt: new Date().toISOString(),
    });
    localStorage.setItem(RECORDS_KEY, JSON.stringify(records));
    showEndModal.value = false;
    sessionMemo.value = '';
  }

  function discardSession() {
    showEndModal.value = false;
    sessionMemo.value = '';
  }

  return {
    isStudying,
    studyStartIndex,
    showEndModal,
    sessionResult,
    sessionMemo,
    startStudy,
    endStudy,
    saveSession,
    discardSession,
  };
});
