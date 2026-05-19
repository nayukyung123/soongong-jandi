import { defineStore } from 'pinia';
import { ref } from 'vue';
import { usePlansStore } from './plans';
import { usePlanTimerStore } from './planTimer';
import { assignGrapeReward } from '../utils/grapeReward.js';

const RECORDS_KEY = 'study_records';

export const useSessionStore = defineStore('session', () => {
  const isStudying = ref(false);
  const studyStartIndex = ref(0);
  const showEndModal = ref(false);
  const sessionResult = ref({});
  /** 공부 기록 모달에서 되돌아가기용 (planTimer undo 또는 study 세션 스냅샷) */
  const endModalUndo = ref(null);
  const studyResumeSnapshot = ref(null);
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
    const { undo, ...sessionData } = result;
    sessionResult.value = sessionData;
    sessionMemo.value = '';
    isStudying.value = false;
    showEndModal.value = true;
    endModalUndo.value = undo ?? null;
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
    endModalUndo.value = null;
    lastGrapeAward.value = grape;
    showGrapeRewardModal.value = true;
  }

  /** 모달에서 타이머/공부 화면으로 복귀 (기록 저장 없음) */
  function returnFromEndModal() {
    const undo = endModalUndo.value;
    showEndModal.value = false;
    sessionMemo.value = '';
    endModalUndo.value = null;

    if (!undo) return;
    if (undo.type === 'planTimer') {
      usePlanTimerStore().restoreAfterStopUndo(undo);
    } else if (undo.type === 'studySession') {
      studyResumeSnapshot.value = undo.snapshot;
      isStudying.value = true;
    }
  }

  function closeGrapeRewardModal() {
    showGrapeRewardModal.value = false;
    lastGrapeAward.value = null;
  }

  /** StudySession 마운트 후 호출 — 스냅샷 소비 */
  function takeStudyResumeSnapshot() {
    const s = studyResumeSnapshot.value;
    studyResumeSnapshot.value = null;
    return s;
  }

  return {
    isStudying,
    studyStartIndex,
    showEndModal,
    sessionResult,
    sessionMemo,
    studyResumeSnapshot,
    showGrapeRewardModal,
    lastGrapeAward,
    studyRecordsRevision,
    startStudy,
    endStudy,
    saveSession,
    returnFromEndModal,
    closeGrapeRewardModal,
    takeStudyResumeSnapshot,
  };
});
