<template>
  <div
    class="relative isolate flex min-h-0 flex-1 flex-col overflow-y-auto px-4 py-5 md:px-8 md:py-6"
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

    <!-- 나무 + 농부(나무 왼쪽 아래) — 높이는 콘텐츠만큼만 쓰고 상단으로 배치 -->
    <div
      class="relative z-[1] mx-auto mt-0 w-full max-w-[min(92vw,22rem)] shrink-0 pb-5 pt-2 md:max-w-[24rem] md:pb-8 md:pt-3"
    >
      <button
        type="button"
        class="absolute bottom-2 -left-2 z-[3] w-[clamp(4.75rem,22vw,7rem)] cursor-pointer rounded-2xl outline-none transition-transform duration-300 ease-out focus-visible:ring-4 focus-visible:ring-brand-300/80 md:bottom-4 md:-left-3 md:w-[clamp(5.25rem,20vw,8rem)]"
        aria-label="공부 기록 히스토리 열기"
        @click="showHistoryModal = true"
      >
        <img
          src="/images/stats/farmer-button.png"
          alt=""
          class="w-full origin-bottom object-contain mix-blend-lighten drop-shadow-[0_8px_20px_rgba(0,0,0,0.2)] transition-all duration-300 ease-out hover:-translate-y-1 hover:scale-[1.06] hover:brightness-[1.05] active:translate-y-0 active:scale-[0.96]"
          width="180"
          height="180"
          decoding="async"
        />
      </button>

      <button
        type="button"
        class="group relative z-[2] mx-auto w-full max-w-[min(88vw,20rem)] cursor-pointer rounded-[2rem] outline-none transition-transform duration-300 ease-out focus-visible:ring-4 focus-visible:ring-brand-300/80 md:max-w-[22rem]"
        aria-label="포도송이 모달 열기"
        @click="showBunchModal = true"
      >
        <img
          src="/images/stats/grape-tree.png"
          alt=""
          class="mx-auto w-[56%] max-w-none origin-bottom object-contain object-bottom mix-blend-lighten drop-shadow-[0_8px_22px_rgba(35,25,45,0.26)] transition-all duration-300 ease-out will-change-transform group-hover:-translate-y-2 group-hover:scale-[1.045] group-hover:brightness-[1.07] group-hover:drop-shadow-[0_18px_42px_rgba(45,28,75,0.38)] group-hover:saturate-[1.08] group-active:translate-y-0 group-active:scale-[0.985] group-active:brightness-[0.98] sm:w-[52%] md:w-[48%]"
          width="416"
          height="520"
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

    <Teleport to="#main-modal-teleport">
      <div
        v-if="showBunchModal"
        class="pointer-events-auto absolute inset-0 z-[2] flex min-h-0 min-w-0 flex-col items-center justify-center overflow-hidden bg-black/45 p-3 backdrop-blur-sm sm:p-4"
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
import { useSessionStore } from '../stores/session.js';

const slotCount = 31;

const showBunchModal = ref(false);
const showHistoryModal = ref(false);

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
