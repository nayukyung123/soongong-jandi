<template>
  <!-- modal: 세로 한도로 스케일 → 모달 안 스크롤 없이 송이 전체 표시 -->
  <div
    :class="
      variant === 'modal'
        ? 'relative mx-auto w-full max-w-[min(100%,29rem)] shrink-0 max-h-[min(72vh,calc(100svh-9.25rem))] sm:max-h-[min(74vh,calc(100svh-9.5rem))] md:max-w-[32rem]'
        : 'relative mx-auto w-full max-w-[min(100%,24rem)] shrink-0 sm:max-w-[26rem]'
    "
  >
    <img
      src="/images/stats/grape-bunch-board.png"
      alt=""
      :class="
        variant === 'modal'
          ? 'pointer-events-none block h-auto max-h-[min(72vh,calc(100svh-9.25rem))] w-full object-contain object-center select-none drop-shadow-lg sm:max-h-[min(74vh,calc(100svh-9.5rem))]'
          : 'pointer-events-none w-full select-none drop-shadow-lg'
      "
      width="448"
      height="520"
      decoding="async"
    />
    <div
      :class="[
        'absolute inset-x-[6.5%] bottom-[9%] top-[23%] flex flex-col items-center justify-center',
        variant === 'modal'
          ? 'gap-y-[0.22rem] sm:gap-y-[0.32rem]'
          : 'gap-y-[0.18rem] sm:gap-y-[0.24rem]',
      ]"
      role="img"
      :aria-label="`포도 슬롯 ${filledSlots}개 채움`"
    >
      <div
        v-for="(row, ri) in slotRows"
        :key="ri"
        :class="[
          'flex justify-center',
          variant === 'modal' ? 'gap-[0.28rem] sm:gap-[0.38rem]' : 'gap-[0.22rem] sm:gap-[0.3rem]',
        ]"
      >
        <div
          v-for="slotIdx in row"
          :key="slotIdx"
          :class="[
            'relative flex items-center justify-center rounded-full border-[3px] border-dashed border-white/95 bg-violet-300/40 shadow-[inset_0_2px_5px_rgba(255,255,255,0.45)]',
            variant === 'modal'
              ? 'size-[clamp(1.85rem,7vmin,3.15rem)] sm:size-[clamp(1.95rem,6.6vmin,3.25rem)] md:size-[3.25rem]'
              : 'size-[clamp(1.55rem,5.8vmin,2.55rem)] sm:size-[clamp(1.65rem,5.4vmin,2.65rem)] md:size-[2.65rem]',
          ]"
          :title="slotTitle(slotIdx)"
        >
          <img
            v-if="grapeSrc(slotIdx)"
            :src="grapeSrc(slotIdx)"
            alt=""
            class="h-[92%] w-[92%] object-contain drop-shadow-md"
            width="72"
            height="72"
          />
          <span
            v-else
            :class="
              variant === 'modal'
                ? 'text-[clamp(0.72rem,4vmin,1rem)] font-black tabular-nums text-white [text-shadow:0_1px_2px_rgba(0,0,0,0.45)]'
                : 'text-[clamp(0.62rem,3.4vmin,0.88rem)] font-black tabular-nums text-white [text-shadow:0_1px_2px_rgba(0,0,0,0.45)]'
            "
          >
            {{ slotIdx + 1 }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { grapeImageForTier } from '../../utils/grapeReward.js';

const props = defineProps({
  /** 저장 시각 오름차순 정렬된 공부 기록 (`grapeTier`, `savedAt`) */
  records: { type: Array, default: () => [] },
  /** 모달 전용: 높이 한도로 이미지 스케일(스크롤 없이 표시) */
  variant: {
    type: String,
    default: 'default',
    validator: (v) => v === 'default' || v === 'modal',
  },
});

/** 송이 안 칸 배치: 4·5·6·6·5·4·1 = 31칸 */
const ROW_LENGTHS = [4, 5, 6, 6, 5, 4, 1];

function buildSlotRows() {
  const rows = [];
  let n = 0;
  for (const len of ROW_LENGTHS) {
    const row = [];
    for (let i = 0; i < len; i += 1) row.push(n++);
    rows.push(row);
  }
  return rows;
}

const slotRows = buildSlotRows();

const filledSlots = computed(() =>
  Math.min(props.records.length, ROW_LENGTHS.reduce((a, b) => a + b, 0))
);

function grapeSrc(slotIdx) {
  const rec = props.records[slotIdx];
  if (!rec) return null;
  return grapeImageForTier(rec.grapeTier);
}

function formatSaved(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

function slotTitle(slotIdx) {
  const rec = props.records[slotIdx];
  if (!rec) return `${slotIdx + 1}번 칸 · 아직 비었어요`;
  return `${slotIdx + 1}번 칸 · ${formatSaved(rec.savedAt)}`;
}
</script>
