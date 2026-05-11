import { defineStore } from 'pinia';
import { ref } from 'vue';
import { usePlansStore } from './plans';
import { assignGrapeReward } from '../utils/grapeReward.js';

const RECORDS_KEY = 'study_records';

export const useSessionStore = defineStore('session', () => {
  const isStudying = ref(false);
  const studyStartIndex = ref(0);
  const showEndModal = ref(false);
  const sessionResult = ref({});
  const sessionMemo = ref('');
  /** 기록 저장 직후 표시할 포도알 보상 (랜덤·추후 API 대체) */
  const showGrapeRewardModal = ref(false);
  const lastGrapeAward = ref(null);
  /** 공부 기록(localStorage) 갱신 시 통계 등에서 다시 읽도록 트리거 */
  const studyRecordsRevision = ref(0);

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
    const grape = assignGrapeReward({
      memo: sessionMemo.value,
      elapsed: sessionResult.value.elapsed
    });
    records.push({
      date: plans.dateKey,
      elapsed: sessionResult.value.elapsed,
      absence: sessionResult.value.absenceTime,
      grapeTier: grape.tierKey,
      memo: sessionMemo.value,
      savedAt: new Date().toISOString(),
    });
    localStorage.setItem(RECORDS_KEY, JSON.stringify(records));
    studyRecordsRevision.value += 1;
    showEndModal.value = false;
    sessionMemo.value = '';
    lastGrapeAward.value = grape;
    showGrapeRewardModal.value = true;
  }

  function discardSession() {
    showEndModal.value = false;
    sessionMemo.value = '';
  }

  function closeGrapeRewardModal() {
    showGrapeRewardModal.value = false;
    lastGrapeAward.value = null;
  }

  return {
    isStudying,
    studyStartIndex,
    showEndModal,
    sessionResult,
    sessionMemo,
    showGrapeRewardModal,
    lastGrapeAward,
    studyRecordsRevision,
    startStudy,
    endStudy,
    saveSession,
    discardSession,
    closeGrapeRewardModal,
  };
});
