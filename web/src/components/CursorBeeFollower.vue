<template>
  <div
    v-show="visible"
    class="pointer-events-none fixed left-0 top-0 z-[10300] select-none"
    :style="wrapperStyle"
    aria-hidden="true"
  >
    <img
      src="/images/cursor/bee-cursor.png"
      alt=""
      width="56"
      height="56"
      class="block h-14 w-14 max-w-none object-contain drop-shadow-sm"
      draggable="false"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';

const visible = ref(false);
const reduceMotion = ref(false);

const targetX = ref(0);
const targetY = ref(0);
const x = ref(0);
const y = ref(0);

/** 포인터 살짝 오른쪽 아래 — 시스템 커서와 겹치지 않게 */
const OFFSET_X = 18;
const OFFSET_Y = 18;
/** 부드러운 추종 강도 (0~1, 클수록 빠르게 따라감) */
const EASE = 0.22;

let rafId = 0;

const wrapperStyle = computed(() => ({
  transform: `translate3d(${x.value + OFFSET_X}px, ${y.value + OFFSET_Y}px, 0)`,
  willChange: 'transform'
}));

function tick() {
  const dx = targetX.value - x.value;
  const dy = targetY.value - y.value;
  x.value += dx * EASE;
  y.value += dy * EASE;
  if (Math.abs(dx) > 0.3 || Math.abs(dy) > 0.3) {
    rafId = requestAnimationFrame(tick);
  } else {
    x.value = targetX.value;
    y.value = targetY.value;
    rafId = 0;
  }
}

function scheduleTick() {
  if (!rafId) rafId = requestAnimationFrame(tick);
}

function onPointerMove(e) {
  if (!visible.value) visible.value = true;
  targetX.value = e.clientX;
  targetY.value = e.clientY;
  if (reduceMotion.value) {
    x.value = targetX.value;
    y.value = targetY.value;
    if (rafId) {
      cancelAnimationFrame(rafId);
      rafId = 0;
    }
  } else {
    scheduleTick();
  }
}

function onPointerLeave() {
  visible.value = false;
}

onMounted(() => {
  reduceMotion.value =
    typeof window !== 'undefined' &&
    window.matchMedia?.('(prefers-reduced-motion: reduce)').matches === true;

  window.addEventListener('pointermove', onPointerMove, { passive: true });
  document.documentElement.addEventListener('mouseleave', onPointerLeave);
});

onUnmounted(() => {
  window.removeEventListener('pointermove', onPointerMove);
  document.documentElement.removeEventListener('mouseleave', onPointerLeave);
  if (rafId) cancelAnimationFrame(rafId);
});
</script>
