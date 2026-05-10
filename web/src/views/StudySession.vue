<template>
  <div class="flex-1 p-6 bg-app-canvas overflow-hidden h-full flex flex-col gap-4">

    <!-- 헤더 -->
    <header class="flex justify-between items-center shrink-0">
      <div>
        <h2 class="text-2xl font-black">⏱️ 공부 중...</h2>
        <p class="text-app-muted text-xs font-bold mt-0.5">현재 작업: {{ currentTask?.title || '항목 없음' }}</p>
      </div>
      <button
        type="button"
        @click="showBatteryInfo = !showBatteryInfo"
        class="flex items-center gap-2 px-4 py-2 rounded-xl border border-leaf/35 bg-leaf-light/25 font-bold text-xs text-leaf transition hover:bg-leaf-light/40"
      >
        🔋 절전 포토 · 5분 주기 관찰
      </button>
    </header>

    <!-- 메인 그리드 -->
    <div class="grid grid-cols-[1fr_320px] gap-4 flex-1 min-h-0">

      <!-- 왼쪽: 타이머 패널 -->
      <div class="bg-white rounded-3xl border border-gray-100 shadow-sm flex flex-col items-center justify-center p-8 relative overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-b from-brand-50/70 to-white pointer-events-none rounded-3xl" />

        <p class="text-xs text-app-muted font-black uppercase tracking-widest mb-4 z-10">현재 시간</p>

        <!-- 메인 타이머 -->
        <div
          class="text-[72px] font-black tracking-tighter tabular-nums z-10 leading-none mb-2 transition-all duration-300"
          :class="isPaused ? 'text-brand-400 animate-pulse' : 'text-app-ink'"
        >
          {{ formattedTime }}
        </div>

        <div v-if="isPaused" class="text-xs text-brand-500 font-black mb-4 z-10">⏸ 일시정지됨</div>

        <p class="text-xs text-app-muted font-bold mb-6 z-10">
          오늘 누적: <span class="text-stem font-black">{{ formattedAccumulated }}</span>
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
                i <= completedGrapes ? 'bg-brand-500 shadow-md scale-110' : 'bg-gray-100'
              ]"
            >
              <span v-if="i <= completedGrapes">🍇</span>
            </div>
          </div>
          <p class="text-sm font-black text-stem">
            {{ completedGrapes }} / {{ totalGrapes }} 알
            <span class="text-app-muted font-bold text-xs ml-2">· 다음까지 {{ minutesToNext }}분</span>
          </p>
        </div>

        <!-- 컨트롤 버튼 -->
        <div class="flex gap-3 z-10">
          <button
            type="button"
            @click="togglePause"
            class="px-6 py-3 bg-white border-2 border-gray-200 rounded-2xl font-black text-sm shadow-sm transition hover:border-brand-300 hover:text-brand-600"
          >
            {{ isPaused ? '▶ 재개하기' : '⏸ 잠시 쉬기' }}
          </button>
          <button
            type="button"
            @click="stopSession"
            class="px-6 py-3 rounded-2xl bg-app-ink font-black text-sm text-white shadow-sm transition hover:bg-brand-700"
          >
            ■ 정지 (기록 작성)
          </button>
        </div>
      </div>

      <!-- 오른쪽 패널 -->
      <div class="flex flex-col gap-3 min-h-0 overflow-y-auto pr-0.5">

        <!-- 📷 카메라 스냅샷 영역 (백엔드 연결 전 placeholder) -->
        <div class="rounded-2xl bg-stem overflow-hidden shadow-sm shrink-0 relative">
          <div class="flex h-36 w-full flex-col items-center justify-center gap-2">
            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-stem-light/35">
              <span class="text-xl">📷</span>
            </div>
            <p class="text-[11px] font-bold text-peach">카메라 미리보기</p>
            <p class="text-[10px] text-peach/85">백엔드 연결 후 활성화</p>
          </div>
          <!-- 다음 확인 카운트다운: 백엔드가 줄 값 표시 자리 -->
          <div class="absolute bottom-2 right-2 rounded-lg bg-black/55 px-2 py-1 font-black text-[10px] text-peach/90">
            다음 확인: {{ nextSnapshotCountdown }}
          </div>
        </div>

        <!-- 배터리 절약 모드 안내 -->
        <div
          :class="[
            'shrink-0 rounded-2xl border-2 p-4 transition-all',
            showBatteryInfo ? 'border-leaf/40 bg-leaf-light/20' : 'border-gray-100 bg-gray-50'
          ]"
        >
          <p class="mb-1 font-black text-xs text-stem">🔋 배터리 절약 모드</p>
          <p class="text-[11px] font-bold leading-relaxed text-app-muted">
            카메라는 5분마다 잠깐 켜져 자리에 없는지 어두운지 확인해요.<br />
            그 외 시간에는 꺼져 있습니다.
          </p>
        </div>

        <!-- 지금 하는 중 -->
        <div class="shrink-0 rounded-2xl border border-gray-100 bg-white p-4 shadow-sm">
          <p class="mb-3 font-black text-[10px] uppercase tracking-widest text-app-muted">지금 하는 중</p>
          <div v-if="currentTask" class="mb-3 flex items-start gap-2">
            <span class="shrink-0 font-black text-sm text-brand-500">✓</span>
            <span class="font-black text-sm text-app-ink">{{ currentTask.title }}</span>
          </div>
          <div v-if="upcomingTasks.length > 0">
            <p class="mb-2 font-bold text-[10px] text-app-muted">다음 →</p>
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
            type="button"
            @click="completeCurrentTask"
            class="mt-3 w-full rounded-xl bg-brand-50 py-2 font-black text-xs text-brand-600 transition hover:bg-brand-100"
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
                  'shrink-0 rounded-lg px-2 py-0.5 font-bold text-[10px]',
                  log.type === 'present' ? 'bg-leaf-light/50 text-leaf' :
                  log.type === 'absent' ? 'bg-blush/80 text-red-700' :
                  log.type === 'resume' ? 'bg-brand-100 text-brand-600' :
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