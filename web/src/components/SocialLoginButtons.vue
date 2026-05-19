<template>
  <div :class="compact ? 'space-y-1' : 'space-y-2'">
    <p v-if="!compact" class="text-[10px] font-bold text-gray-400 px-2">소셜 로그인</p>
    <button
      type="button"
      :class="[
        'flex w-full items-center justify-center font-black transition',
        compact
          ? 'gap-1 rounded-lg bg-[#FEE500] px-1.5 py-1 text-[9px] leading-tight text-[#191919] shadow-sm hover:brightness-95'
          : 'gap-2 rounded-2xl bg-[#FEE500] px-4 py-2.5 text-sm text-[#191919] shadow-sm hover:brightness-95',
      ]"
      @click="goKakao"
    >
      kakao
    </button>
    <button
      type="button"
      :class="[
        'flex w-full items-center justify-center font-black transition',
        compact
          ? 'gap-1 rounded-lg border border-gray-300 bg-white/95 px-1.5 py-1 text-[9px] leading-tight text-gray-700 hover:bg-white'
          : 'gap-2 rounded-2xl border-2 border-gray-200 bg-white px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50',
      ]"
      @click="goGoogle"
    >
      google
    </button>
  </div>
</template>

<script setup>
import { getOAuthLoginUrl } from '../config/oauth.js';

defineProps({
  compact: { type: Boolean, default: false },
});

function goKakao() {
  window.location.href = getOAuthLoginUrl('kakao');
}

function goGoogle() {
  window.location.href = getOAuthLoginUrl('google');
}
</script>
