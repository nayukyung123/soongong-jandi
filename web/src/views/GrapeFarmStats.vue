<template>
  <div
    class="relative isolate flex min-h-0 flex-1 flex-col overflow-x-hidden overflow-y-auto px-4 py-5 md:px-8 md:py-6"
  >
    <!-- 농장 일러스트 배경 -->
    <div
      class="pointer-events-none absolute inset-0 -z-10 bg-[url('/images/stats/farm-background.png')] bg-cover bg-center bg-no-repeat"
      aria-hidden="true"
    />
    <!-- 배경 위 살짝 밝게 (상단 텍스트 없음 — 나무가 있는 아래는 농장이 잘 보이도록) -->
    <div
      class="pointer-events-none absolute inset-0 -z-[9] bg-gradient-to-b from-white/35 via-white/15 to-transparent"
      aria-hidden="true"
    />

    <!-- 빨간 지붕 집 ↔ 보라 지붕 창고 (갈색 밭 가장자리) -->
    <div class="stats-home-walker" aria-hidden="true">
      <div class="stats-home-walker__sprite">
        <div class="stats-home-walker__facing">
          <div class="stats-home-walker__stride">
          <img
            src="/images/stats/farmer-walker-right.png"
            alt=""
            class="stats-home-walker__character stats-home-walker__character--right"
            width="180"
            height="180"
            decoding="async"
          />
          <img
            src="/images/stats/farmer-walker-left.png"
            alt=""
            class="stats-home-walker__character stats-home-walker__character--left"
            width="180"
            height="180"
            decoding="async"
          />
          </div>
        </div>
      </div>
    </div>

    <header class="stats-home-user-panel relative z-[6] shrink-0 self-start">
      <div class="stats-home-user-panel__card">
        <div class="flex items-start justify-between gap-3">
          <div class="flex min-w-0 items-center gap-3">
            <span
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-brand-50 text-2xl leading-none"
              aria-hidden="true"
            >🍇</span>
            <div class="min-w-0">
              <p class="text-[10px] font-black uppercase tracking-wide text-brand-600">
                포도공부
              </p>
              <h1 class="truncate text-base font-black text-gray-900">
                {{ userProfile.name }}
              </h1>
              <p v-if="isAuthenticated" class="mt-0.5 text-xs font-bold text-gray-500">
                Lv.{{ userProfile.level }}
              </p>
              <p v-else class="mt-0.5 text-xs font-bold text-gray-500">
                게스트 · 로그인 시 기록 저장
              </p>
            </div>
          </div>
          <SocialLoginButtons v-if="!isAuthenticated" class="shrink-0" />
          <button
            v-else
            type="button"
            class="shrink-0 rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-xs font-black text-gray-600 transition hover:bg-white"
            @click="logout"
          >
            로그아웃
          </button>
        </div>
      </div>
    </header>

    <!-- 나무 + 농부 + 달력 이동 -->
    <div class="stats-home-stage">
      <button
        type="button"
        class="stats-home-stage__farmer"
        aria-label="공부 기록 히스토리 열기"
        @click="openHistoryModal"
      >
        <img
          src="/images/stats/farmer-button.png"
          alt=""
          class="stats-home-stage__img"
          width="180"
          height="180"
          decoding="async"
        />
      </button>

      <button
        type="button"
        class="stats-home-stage__tree group"
        aria-label="포도송이 모달 열기"
        @click="openBunchModal"
      >
        <img
          src="/images/stats/grape-tree.png"
          alt=""
          class="stats-home-stage__tree-img"
          width="416"
          height="520"
          decoding="async"
        />
      </button>

      <button
        type="button"
        class="stats-home-stage__calendar"
        aria-label="달력으로 이동"
        @click.stop="goCalendar"
      >
        <img
          src="/images/stats/calendar-go-button.png"
          alt=""
          class="stats-home-stage__img"
          width="88"
          height="88"
          decoding="async"
        />
      </button>
    </div>

    <StudyHistoryModal v-model:show="showHistoryModal" />

    <section
      v-if="recentOverflowRecords.length"
      class="relative z-[1] mx-auto mt-6 w-full max-w-2xl rounded-2xl border border-dashed border-brand-200 bg-brand-50/50 px-4 py-3 text-center"
    >
      <p class="text-[11px] font-black text-brand-800">
        송이 밖 기록 {{ recentOverflowRecords.length }}건 (최근 순 최대 8개만 표시)
      </p>
      <ul class="mt-2 space-y-1 text-left text-[11px] font-bold text-gray-600">
        <li v-for="(rec, i) in recentOverflowRecords" :key="i">
          {{ formatSaved(rec.savedAt) }}
          ·
          {{ tierLabel(rec.grapeTier) }}
        </li>
      </ul>
    </section>

    <Teleport to="body">
      <div
        v-if="showBunchModal"
        class="fixed inset-0 z-[10190] flex min-h-0 min-w-0 flex-col items-center justify-center overflow-hidden bg-black/45 p-3 backdrop-blur-sm sm:p-4"
        role="dialog"
        aria-modal="true"
        aria-labelledby="grape-bunch-modal-title"
        @click.self="showBunchModal = false"
      >
        <div
          class="flex max-h-[min(92vh,820px)] min-h-0 w-full max-w-xl flex-col items-stretch overflow-hidden rounded-3xl bg-white p-3 shadow-2xl sm:p-4 md:p-5"
          @click.stop
        >
          <div class="mb-2 flex w-full shrink-0 items-start justify-between gap-3 border-b border-gray-100 pb-2 sm:mb-3 sm:pb-3">
            <div class="min-w-0 text-left">
              <p class="text-[11px] font-black uppercase tracking-wide text-brand-600">포도송이</p>
              <h2 id="grape-bunch-modal-title" class="mt-0.5 text-lg font-black leading-snug text-gray-900 sm:text-xl">
                내 포도 송이
              </h2>
            </div>
            <button
              type="button"
              class="shrink-0 rounded-2xl px-3 py-2 text-sm font-bold text-gray-500 transition hover:bg-gray-100"
              @click="showBunchModal = false"
            >
              닫기
            </button>
          </div>
          <div
            class="flex min-h-0 w-full min-w-0 flex-1 flex-col items-center justify-center overflow-hidden px-0.5 pb-1 pt-1"
          >
            <GrapeBunchBoard :records="sortedRecords" variant="modal" />
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { storeToRefs } from 'pinia';
import GrapeBunchBoard from '../components/grapeFarm/GrapeBunchBoard.vue';
import StudyHistoryModal from '../components/grapeFarm/StudyHistoryModal.vue';
import SocialLoginButtons from '../components/SocialLoginButtons.vue';
import { useSessionStore } from '../stores/session.js';
import { useUiStore } from '../stores/ui.js';
import { useAuthStore } from '../stores/auth.js';
import { decodeJwtPayload } from '../utils/jwtPayload.js';

const uiStore = useUiStore();
const authStore = useAuthStore();
const { isAuthenticated } = storeToRefs(authStore);

const userProfile = computed(() => {
  if (!authStore.token) {
    return { name: '게스트', level: '–' };
  }
  const p = decodeJwtPayload(authStore.token);
  const name =
    (typeof p?.name === 'string' && p.name) ||
    (typeof p?.nickname === 'string' && p.nickname) ||
    '사용자';
  const level = p?.level != null ? String(p.level) : '3';
  return { name, level };
});

function logout() {
  authStore.clearToken();
}

const slotCount = 31;

const showBunchModal = ref(false);
const showHistoryModal = ref(false);

function openBunchModal() {
  showHistoryModal.value = false;
  showBunchModal.value = true;
}

function openHistoryModal() {
  showBunchModal.value = false;
  showHistoryModal.value = true;
}

function goCalendar() {
  showBunchModal.value = false;
  showHistoryModal.value = false;
  uiStore.goCalendar();
}

const sessionStore = useSessionStore();
const { studyRecordsRevision } = storeToRefs(sessionStore);

const sortedRecords = computed(() => {
  studyRecordsRevision.value;
  try {
    const raw = localStorage.getItem('study_records');
    const arr = JSON.parse(raw || '[]');
    if (!Array.isArray(arr)) return [];
    return [...arr].sort(
      (a, b) =>
        new Date(a.savedAt || 0).getTime() - new Date(b.savedAt || 0).getTime()
    );
  } catch {
    return [];
  }
});

function formatSaved(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function tierLabel(tierKey) {
  if (tierKey === 'insufficient') return '미흡';
  if (tierKey === 'best') return '가장 열심히 함';
  return '보통';
}

const recentOverflowRecords = computed(() => {
  const list = sortedRecords.value;
  if (list.length <= slotCount) return [];
  return list
    .slice(slotCount)
    .reverse()
    .slice(0, 8);
});
</script>

<style scoped>
/*
 * 홈 버튼: 위치는 rem/% 고정, 크기만 min(최대, cqw)로 영역이 줄 때 축소.
 * cqw = stats-home-stage 너비 기준 (최대 22rem ≈ 큰 화면 default).
 */
.stats-home-stage {
  position: relative;
  z-index: 10;
  box-sizing: border-box;
  container-type: inline-size;
  container-name: stats-home-stage;
  width: min(92vw, 22rem);
  max-width: 100%;
  margin-inline: auto;
  margin-top: -2.75rem;
  flex-shrink: 0;
  min-height: 18.5rem;
  padding-bottom: 1.5rem;
}

@media (min-width: 768px) {
  .stats-home-stage {
    width: min(92vw, 24rem);
    margin-top: -3rem;
    min-height: 20rem;
    padding-bottom: 2rem;
  }
}

.stats-home-stage__farmer,
.stats-home-stage__tree,
.stats-home-stage__calendar {
  margin: 0;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  outline: none;
  transition: transform 0.3s ease, filter 0.3s ease;
}

.stats-home-stage__farmer:focus-visible,
.stats-home-stage__tree:focus-visible,
.stats-home-stage__calendar:focus-visible {
  outline: 2px solid rgba(111, 63, 175, 0.55);
  outline-offset: 2px;
}

/* 공부 히스토리 버튼 — 기존과 동일(고정 위치, 애니메이션 없음) */
.stats-home-stage__farmer {
  position: absolute;
  z-index: 12;
  bottom: 2.25rem;
  left: -3.75rem;
  width: min(8rem, 36cqw);
}

.stats-home-stage__farmer:hover {
  transform: translateY(-2px) scale(1.05);
  filter: brightness(1.05);
}

/* 좌측 상단 사용자 정보 — 배경 위 가독용 흰 박스 */
.stats-home-user-panel {
  width: min(100%, 22rem);
}

.stats-home-user-panel__card {
  border-radius: 1rem;
  border: 1px solid rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.96);
  padding: 0.875rem 1rem;
  box-shadow:
    0 4px 20px rgba(35, 25, 45, 0.12),
    0 1px 0 rgba(255, 255, 255, 0.8) inset;
}

@media (min-width: 768px) {
  .stats-home-user-panel__card {
    padding: 1rem 1.125rem;
  }
}

/*
 * 순찰 캐릭터 — L자 동선(대각 ↙ 후 →), linear로 구간별 직선 이동
 */
.stats-home-walker {
  position: absolute;
  inset: 0;
  z-index: 4;
  overflow: hidden;
  pointer-events: none;
}

.stats-home-walker__sprite {
  position: absolute;
  width: min(4.25rem, 11vw);
  transform: translate(-50%, -100%);
  animation:
    walker-between-houses 20s linear infinite alternate,
    walker-fade-cycle 40s ease-in-out infinite;
  will-change: left, top, opacity;
}

/* 왕복(20s×2) 시 돌아올 때 좌우 반전 — 이동 애니메이션과 동기(40s) */
.stats-home-walker__facing {
  transform-origin: center bottom;
  animation: walker-return-flip 40s linear infinite;
}

@keyframes walker-return-flip {
  0%,
  49.99% {
    transform: scaleX(1);
  }
  50%,
  100% {
    transform: scaleX(-1);
  }
}

.stats-home-walker__stride {
  position: relative;
  width: 100%;
}

.stats-home-walker__character {
  display: block;
  width: 100%;
  height: auto;
  object-fit: contain;
  object-position: bottom center;
  mix-blend-mode: lighten;
  filter: drop-shadow(0 6px 14px rgba(0, 0, 0, 0.18));
}

.stats-home-walker__character--left {
  position: absolute;
  inset: 0;
  animation: walker-stride-left 0.55s steps(1, end) infinite;
}

.stats-home-walker__character--right {
  animation: walker-stride-right 0.55s steps(1, end) infinite;
}

@keyframes walker-stride-right {
  0%,
  49% {
    opacity: 1;
  }
  50%,
  100% {
    opacity: 0;
  }
}

@keyframes walker-stride-left {
  0%,
  49% {
    opacity: 0;
  }
  50%,
  100% {
    opacity: 1;
  }
}

/*
 * 구간별 이동 거리에 비례해 타임라인 배분 → 체감 속도 균일
 * ① 문 앞 한 걸음 ② ↙ 직선 ③ → 직선(보라 집)
 */
@keyframes walker-between-houses {
  0% {
    left: 33%;
    top: 38%;
  }
  /* 앞으로 한 걸음 (~5% 구간) */
  8% {
    left: 36%;
    top: 41%;
  }
  /* 왼쪽 아래 꼭짓점 — ↙ 직선 종료 */
  37% {
    left: 20%;
    top: 68%;
  }
  /* 오른쪽 아래 → 보라 집 (화면 밖·스크롤 없이, 끝보다 안쪽) */
  100% {
    left: 76%;
    top: 96%;
  }
}

/*
 * 페이드 — 이동(20s×2)과 40s 동기
 * 가는 길: 빨간 집에서 등장 → 보라 집에서 사라짐
 * 오는 길: 보라 집에서 등장 → 빨간 집에서는 사라지지 않음
 */
@keyframes walker-fade-cycle {
  0% {
    opacity: 0;
  }
  7% {
    opacity: 1;
  }
  43% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
  57% {
    opacity: 1;
  }
  100% {
    opacity: 1;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stats-home-walker__sprite,
  .stats-home-walker__facing {
    animation: none;
    opacity: 1;
  }

  .stats-home-walker__facing {
    transform: none;
  }

  .stats-home-walker__character--left {
    display: none;
    animation: none;
  }

  .stats-home-walker__character--right {
    animation: none;
  }
}

/* 나무 — 위치 고정, 큰 화면에서 이미지 약 56% × 20rem */
.stats-home-stage__tree {
  position: relative;
  z-index: 11;
  display: block;
  width: 100%;
  max-width: 20rem;
  margin-inline: auto;
  transform: translateY(-2.75rem);
}

@media (min-width: 768px) {
  .stats-home-stage__tree {
    max-width: 22rem;
    transform: translateY(-3rem);
  }
}

.stats-home-stage__tree:hover {
  transform: translateY(-2.9rem) scale(1.02);
  filter: brightness(1.05);
}

@media (min-width: 768px) {
  .stats-home-stage__tree:hover {
    transform: translateY(-3.15rem) scale(1.02);
  }
}

.stats-home-stage__tree-img {
  display: block;
  width: 56%;
  max-width: none;
  margin-inline: auto;
  object-fit: contain;
  object-position: bottom center;
  mix-blend-mode: lighten;
  filter: drop-shadow(0 8px 22px rgba(35, 25, 45, 0.26));
  transition: transform 0.3s ease, filter 0.3s ease;
}

.stats-home-stage__tree:hover .stats-home-stage__tree-img {
  transform: translateY(-4px) scale(1.03);
  filter: drop-shadow(0 14px 32px rgba(45, 28, 75, 0.32)) saturate(1.06);
}

/* 달력 — 위치 고정, 큰 화면 default 5.5rem */
.stats-home-stage__calendar {
  position: absolute;
  z-index: 30;
  bottom: 14%;
  left: 74%;
  width: min(5.5rem, 26cqw);
}

.stats-home-stage__calendar:hover {
  transform: scale(1.05);
}

.stats-home-stage__img {
  display: block;
  width: 100%;
  height: auto;
  object-fit: contain;
  mix-blend-mode: lighten;
  filter: drop-shadow(0 8px 18px rgba(0, 0, 0, 0.22));
}

.stats-home-stage__calendar .stats-home-stage__img {
  filter: drop-shadow(0 10px 24px rgba(45, 28, 75, 0.35));
}
</style>
