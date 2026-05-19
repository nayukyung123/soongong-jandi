<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 z-[10190] flex min-h-0 min-w-0 flex-col items-center justify-center overflow-x-hidden overflow-y-auto bg-black/45 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="study-history-modal-title"
      @click.self="close"
    >
      <div
        class="flex max-h-[min(92vh,820px)] w-full max-w-lg flex-col overflow-hidden rounded-3xl bg-white shadow-2xl"
        @click.stop
      >
        <header
          class="flex shrink-0 items-start justify-between gap-3 border-b border-gray-100 px-5 pb-4 pt-5 sm:px-6"
        >
          <div class="min-w-0">
            <p class="text-[11px] font-black uppercase tracking-wide text-brand-600">공부 기록</p>
            <h2 id="study-history-modal-title" class="mt-1 text-xl font-black text-gray-900">
              제출한 내용 히스토리
            </h2>
            <p class="mt-1 text-xs font-bold text-gray-500">
              날짜별로 저장해 둔 공부 메모와 시간을 볼 수 있어요.
            </p>
          </div>
          <button
            type="button"
            class="shrink-0 rounded-2xl px-3 py-2 text-sm font-bold text-gray-500 transition hover:bg-gray-100"
            @click="close"
          >
            닫기
          </button>
        </header>

        <div class="min-h-0 flex-1 overflow-y-auto overscroll-contain px-5 pb-5 pt-2 sm:px-6">
          <template v-if="groupedByDate.length">
            <section
              v-for="block in groupedByDate"
              :key="block.dateKey"
              class="mb-6 last:mb-2"
            >
              <h3
                class="sticky top-0 z-[1] -mx-1 mb-3 bg-white/95 px-1 py-1 text-sm font-black text-brand-800 backdrop-blur-sm"
              >
                {{ formatDateHeading(block.dateKey) }}
              </h3>
              <ul class="space-y-3">
                <li
                  v-for="(rec, idx) in block.items"
                  :key="`${block.dateKey}-${idx}-${rec.savedAt}`"
                  class="rounded-2xl border border-gray-100 bg-gray-50/90 p-3.5 shadow-sm"
                >
                  <div class="flex flex-wrap items-center gap-x-2 gap-y-1 text-[11px] font-bold text-gray-500">
                    <time class="tabular-nums text-gray-700">{{ formatTime(rec.savedAt) }}</time>
                    <span class="text-gray-300">·</span>
                    <span class="text-brand-700">실공부 {{ formatSecondsShort(rec.elapsed) }}</span>
                    <span v-if="rec.absence != null && rec.absence > 0" class="text-gray-400">
                      자리비움 {{ formatSecondsShort(rec.absence) }}
                    </span>
                    <span
                      class="ml-auto rounded-lg bg-brand-100 px-2 py-0.5 text-[10px] font-black text-brand-800"
                    >
                      {{ tierLabel(rec.grapeTier) }}
                    </span>
                  </div>
                  <p class="mt-2 whitespace-pre-wrap text-sm font-bold leading-relaxed text-gray-800">
                    {{ rec.memo?.trim() || '작성한 내용이 없어요.' }}
                  </p>
                </li>
              </ul>
            </section>
          </template>
          <div
            v-else
            class="flex min-h-[200px] flex-col items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-gray-200 bg-gray-50/80 px-6 py-10 text-center"
          >
            <p class="text-3xl" aria-hidden="true">📝</p>
            <p class="text-sm font-black text-gray-500">아직 저장된 공부 기록이 없어요.</p>
            <p class="text-xs font-bold text-gray-400">공부를 마치고 「기록 저장」을 하면 여기에 쌓여요.</p>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useSessionStore } from '../../stores/session.js';
import { formatSecondsShort } from '../../utils/formatSession.js';

defineProps({
  show: { type: Boolean, default: false },
});

const emit = defineEmits(['update:show']);

function close() {
  emit('update:show', false);
}

const sessionStore = useSessionStore();
const { studyRecordsRevision } = storeToRefs(sessionStore);

const records = computed(() => {
  studyRecordsRevision.value;
  try {
    const raw = localStorage.getItem('study_records');
    const arr = JSON.parse(raw || '[]');
    if (!Array.isArray(arr)) return [];
    return arr;
  } catch {
    return [];
  }
});

function dateKeyForRecord(rec) {
  if (rec.date && /^\d{4}-\d{2}-\d{2}$/.test(String(rec.date))) return String(rec.date);
  const d = new Date(rec.savedAt);
  if (Number.isNaN(d.getTime())) return '기타';
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/** 최신 날짜가 위로 오도록 정렬, 같은 날은 저장 시각 내림차순 */
const groupedByDate = computed(() => {
  const map = new Map();
  for (const rec of records.value) {
    const key = dateKeyForRecord(rec);
    if (!map.has(key)) map.set(key, []);
    map.get(key).push(rec);
  }
  for (const [, items] of map) {
    items.sort(
      (a, b) =>
        new Date(b.savedAt || 0).getTime() - new Date(a.savedAt || 0).getTime()
    );
  }
  const keys = [...map.keys()].sort((a, b) => b.localeCompare(a));
  return keys.map((dateKey) => ({ dateKey, items: map.get(dateKey) }));
});

function formatDateHeading(dateKey) {
  if (dateKey === '기타') return '날짜 미상';
  const p = dateKey.split('-');
  if (p.length !== 3) return dateKey;
  const [y, m, d] = p.map((x) => parseInt(x, 10));
  if (Number.isNaN(y)) return dateKey;
  return `${y}년 ${m}월 ${d}일`;
}

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function tierLabel(tierKey) {
  if (tierKey === 'insufficient') return '미흡';
  if (tierKey === 'best') return '가장 열심히 함';
  return '보통';
}
</script>
