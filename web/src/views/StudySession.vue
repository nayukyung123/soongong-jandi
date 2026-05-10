<template>
  <div class="flex-1 p-6 bg-[#F8F9FA] overflow-hidden h-full flex flex-col gap-4">

    <!-- 헤더 -->
    <header class="flex justify-between items-center shrink-0">
      <div>
        <h2 class="text-2xl font-black">⏱️ 공부 중...</h2>
        <p class="text-gray-400 text-xs font-bold mt-0.5">현재 작업: {{ currentTask?.title || '항목 없음' }}</p>
      </div>
      <button
        @click="showBatteryInfo = !showBatteryInfo"
        class="flex items-center gap-2 px-4 py-2 bg-green-50 border border-green-200 text-green-700 rounded-xl font-bold text-xs hover:bg-green-100 transition"
      >
        🔋 절전 포토 · 5분 주기 관찰
      </button>
    </header>

    <!-- 메인 그리드 -->
    <div class="grid grid-cols-[1fr_320px] gap-4 flex-1 min-h-0">

      <!-- 왼쪽: 타이머 패널 -->
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm flex flex-col items-center justify-center p-8 relative overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-b from-purple-50/60 to-white pointer-events-none rounded-3xl" />

        <p class="text-xs text-gray-400 font-black uppercase tracking-widest mb-4 z-10">현재 시간</p>

        <!-- 메인 타이머 -->
        <div
          class="text-[72px] font-black tracking-tighter tabular-nums z-10 leading-none mb-2 transition-all duration-300"
          :class="isPaused ? 'text-purple-400 animate-pulse' : 'text-gray-800'"
        >
          {{ formattedTime }}
        </div>

        <div v-if="isPaused" class="text-xs text-purple-400 font-black mb-4 z-10">⏸ 일시정지됨</div>

        <p class="text-xs text-gray-400 font-bold mb-6 z-10">
          오늘 누적: <span class="text-gray-600 font-black">{{ formattedAccumulated }}</span>
          · 자리비움: <span class="text-red-400 font-black">{{ formattedAbsence }} ({{ absenceCount }}회)</span>
        </p>

        <!-- 포도알 시각화 -->
        <div class="z-10 flex flex-col items-center mb-6">
          <div class="flex flex-wrap justify-center gap-2 max-w-[200px] mb-3">
            <div
              v-for="i in totalGrapes"
              :key="i"
              :class="[
                'w-9 h-9 rounded-full flex items-center justify-center text-lg transition-all duration-500',
                i <= completedGrapes ? 'bg-purple-500 shadow-md scale-110' : 'bg-gray-100'
              ]"
            >
              <span v-if="i <= completedGrapes">🍇</span>
            </div>
          </div>
          <p class="text-sm font-black text-gray-600">
            {{ completedGrapes }} / {{ totalGrapes }} 알
            <span class="text-gray-400 font-bold text-xs ml-2">· 다음까지 {{ minutesToNext }}분</span>
          </p>
        </div>

        <!-- 컨트롤 버튼 -->
        <div class="flex gap-3 z-10">
          <button
            @click="togglePause"
            class="px-6 py-3 bg-white border-2 border-gray-200 rounded-2xl font-black text-sm hover:border-purple-300 hover:text-purple-600 transition shadow-sm"
          >
            {{ isPaused ? '▶ 재개하기' : '⏸ 잠시 쉬기' }}
          </button>
          <button
            @click="stopSession"
            class="px-6 py-3 bg-gray-800 text-white rounded-2xl font-black text-sm hover:bg-gray-900 transition shadow-sm"
          >
            ■ 정지 (기록 작성)
          </button>
        </div>
      </div>

      <!-- 오른쪽 패널 -->
      <div class="flex flex-col gap-3 min-h-0 overflow-y-auto pr-0.5">

        <!-- 📷 카메라 스냅샷 영역 (백엔드 연결 전 placeholder) -->
        <div class="bg-gray-800 rounded-2xl overflow-hidden shadow-sm shrink-0 relative">
          <div class="w-full h-36 flex flex-col items-center justify-center gap-2">
            <div class="w-10 h-10 rounded-full bg-gray-700 flex items-center justify-center">
              <span class="text-xl">📷</span>
            </div>
            <p class="text-gray-500 text-[11px] font-bold">카메라 미리보기</p>
            <p class="text-gray-600 text-[10px]">백엔드 연결 후 활성화</p>
          </div>
          <!-- 다음 확인 카운트다운: 백엔드가 줄 값 표시 자리 -->
          <div class="absolute bottom-2 right-2 bg-black/60 text-gray-400 text-[10px] font-black px-2 py-1 rounded-lg">
            다음 확인: {{ nextSnapshotCountdown }}
          </div>
        </div>

        <!-- 배터리 절약 모드 안내 -->
        <div
          :class="['rounded-2xl border-2 p-4 shrink-0 transition-all', showBatteryInfo ? 'bg-green-50 border-green-200' : 'bg-gray-50 border-gray-100']"
        >
          <p class="text-xs font-black text-gray-700 mb-1">🔋 배터리 절약 모드</p>
          <p class="text-[11px] text-gray-500 font-bold leading-relaxed">
            카메라는 5분마다 잠깐 켜져 자리에 없는지 어두운지 확인해요.<br />
            그 외 시간에는 꺼져 있습니다.
          </p>
        </div>

        <!-- 지금 하는 중 -->
        <div class="bg-white rounded-2xl border border-gray-100 p-4 shadow-sm shrink-0">
          <p class="text-[10px] text-gray-400 font-black uppercase tracking-widest mb-3">지금 하는 중</p>
          <div v-if="currentTask" class="flex items-start gap-2 mb-3">
            <span class="text-purple-500 font-black text-sm shrink-0">✓</span>
            <span class="font-black text-sm text-gray-800">{{ currentTask.title }}</span>
          </div>
          <div v-if="upcomingTasks.length > 0">
            <p class="text-[10px] text-gray-400 font-bold mb-2">다음 →</p>
            <div
              v-for="task in upcomingTasks.slice(0, 3)"
              :key="task.id"
              class="text-xs text-gray-500 font-bold py-1 pl-3 border-l-2 border-gray-100"
            >
              · {{ task.title }}
            </div>
          </div>
          <button
            v-if="currentTask"
            @click="completeCurrentTask"
            class="w-full mt-3 py-2 bg-purple-50 text-purple-600 rounded-xl font-black text-xs hover:bg-purple-100 transition"
          >
            ✓ 완료 → 다음 항목
          </button>
        </div>

        <!-- 관찰 로그 -->
        <div class="bg-white rounded-2xl border border-gray-100 p-4 shadow-sm flex-1 min-h-0 overflow-hidden flex flex-col">
          <p class="text-[10px] text-gray-400 font-black uppercase tracking-widest mb-3 shrink-0">관찰 로그</p>
          <div class="overflow-y-auto flex-1 space-y-1.5">
            <div
              v-for="(log, i) in observationLogs"
              :key="i"
              class="flex items-center gap-2 text-xs"
            >
              <span class="text-gray-300 font-bold shrink-0 tabular-nums">{{ log.time }}</span>
              <span
                :class="[
                  'font-bold px-2 py-0.5 rounded-lg text-[10px] shrink-0',
                  log.type === 'present'  ? 'bg-green-100 text-green-600'  :
                  log.type === 'absent'   ? 'bg-red-100 text-red-500'      :
                  log.type === 'resume'   ? 'bg-purple-100 text-purple-600':
                                            'bg-gray-100 text-gray-500'
                ]"
              >
                {{ log.type === 'present' ? '✓ 있음' :
                   log.type === 'absent'  ? '✗ 비어있음' :
                   log.type === 'resume'  ? '▶ 재개' : '⏸ 일시정지' }}
              </span>
              <span v-if="log.duration" class="text-gray-400">({{ log.duration }})</span>
            </div>
            <div v-if="observationLogs.length === 0" class="text-xs text-gray-300 font-bold text-center py-4">
              아직 기록이 없어요
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';

const props = defineProps(['allPlans', 'currentDate', 'startTaskIndex']);
const emit = defineEmits(['update:allPlans', 'session-end']);

// ─── 상태 ────────────────────────────────────────────
const elapsed      = ref(0);   // 공부 경과 (초)
const accumulated  = ref(0);   // 오늘 누적 (초)
const absenceTime  = ref(0);   // 자리비움 누적 (초)
const absenceCount = ref(0);
const isPaused     = ref(false);
const showBatteryInfo = ref(false);
const observationLogs = ref([]);

// 다음 스냅샷까지 카운트다운 (5분 = 300초) — 표시용만, 실제 촬영은 백엔드 담당
const nextSnapshotSec = ref(300);

const currentTaskIdx = ref(props.startTaskIndex ?? 0);

// ─── 날짜 키 & 계획 ──────────────────────────────────
const dateKey = computed(() => {
  const d = props.currentDate;
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
});

const plans = computed({
  get: () => props.allPlans[dateKey.value] || [],
  set: (val) => emit('update:allPlans', { ...props.allPlans, [dateKey.value]: val })
});

const currentTask    = computed(() => plans.value[currentTaskIdx.value] || null);
const upcomingTasks  = computed(() => plans.value.slice(currentTaskIdx.value + 1));
const completedGrapes = computed(() => plans.value.filter(p => p.completed).length);
const totalGrapes    = computed(() => Math.max(5, plans.value.length));
const minutesToNext  = computed(() => upcomingTasks.value[0]?.minutes || 0);

// ─── 포맷 헬퍼 ───────────────────────────────────────
const pad = (n) => String(n).padStart(2, '0');

const formattedTime = computed(() => {
  const h = Math.floor(elapsed.value / 3600);
  const m = Math.floor((elapsed.value % 3600) / 60);
  const s = elapsed.value % 60;
  return `${pad(h)}:${pad(m)}:${pad(s)}`;
});

const formattedAccumulated = computed(() => {
  const h = Math.floor(accumulated.value / 3600);
  const m = Math.floor((accumulated.value % 3600) / 60);
  return h > 0 ? `${h}시간 ${m}분` : `${m}분`;
});

const formattedAbsence = computed(() => {
  const m = Math.floor(absenceTime.value / 60);
  const s = absenceTime.value % 60;
  return m > 0 ? `${m}분 ${s}초` : `${s}초`;
});

const nextSnapshotCountdown = computed(() => {
  const m = Math.floor(nextSnapshotSec.value / 60);
  const s = nextSnapshotSec.value % 60;
  return `${pad(m)}:${pad(s)}`;
});

// ─── 타이머 ──────────────────────────────────────────
let timerInterval = null;

const startTimer = () => {
  timerInterval = setInterval(() => {
    if (!isPaused.value) {
      elapsed.value++;
      accumulated.value++;
      // 카운트다운만 내림 (실제 촬영은 백엔드가 담당)
      nextSnapshotSec.value = nextSnapshotSec.value <= 0 ? 300 : nextSnapshotSec.value - 1;
    } else {
      absenceTime.value++;
    }
  }, 1000);
};

const stopTimer = () => {
  clearInterval(timerInterval);
  timerInterval = null;
};

// ─── 일시정지 / 재개 ─────────────────────────────────
const getTimeStr = () => {
  const now = new Date();
  return `${pad(now.getHours())}:${pad(now.getMinutes())}`;
};

const addLog = (type, time, duration) => {
  observationLogs.value.unshift({ type, time, duration });
  if (observationLogs.value.length > 50) observationLogs.value.pop();
};

const togglePause = () => {
  const now = getTimeStr();
  if (!isPaused.value) {
    isPaused.value = true;
    absenceCount.value++;
    addLog('pause', now);
  } else {
    isPaused.value = false;
    addLog('resume', now);
  }
};

// ─── 항목 완료 ───────────────────────────────────────
const completeCurrentTask = () => {
  if (!currentTask.value) return;
  plans.value = plans.value.map((p, i) =>
    i === currentTaskIdx.value ? { ...p, completed: true } : p
  );
  if (currentTaskIdx.value < plans.value.length - 1) {
    currentTaskIdx.value++;
  }
};

// ─── 세션 종료 ───────────────────────────────────────
const stopSession = () => {
  stopTimer();
  emit('session-end', {
    elapsed:         elapsed.value,
    accumulated:     accumulated.value,
    absenceTime:     absenceTime.value,
    absenceCount:    absenceCount.value,
    completedGrapes: completedGrapes.value
  });
};

// ─── 라이프사이클 ────────────────────────────────────
onMounted(() => {
  startTimer();
  addLog('present', getTimeStr());
});

onUnmounted(() => {
  stopTimer();
});
</script>

<style scoped>
.tabular-nums { font-variant-numeric: tabular-nums; }
</style>