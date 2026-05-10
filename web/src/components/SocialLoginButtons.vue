<template>
  <div class="space-y-2">
    <p class="text-[10px] font-bold text-gray-400 px-2">소셜 로그인</p>
    <button
      type="button"
      class="flex w-full items-center justify-center gap-2 rounded-2xl bg-[#FEE500] px-4 py-2.5 text-sm font-black text-[#191919] shadow-sm hover:brightness-95 transition"
      @click="goKakao"
    >
      카카오로 계속하기
    </button>
    <button
      type="button"
      class="flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-gray-200 bg-white px-4 py-2.5 text-sm font-black text-gray-700 hover:bg-gray-50 transition"
      @click="goGoogle"
    >
      Google로 계속하기
    </button>
  </div>
</template>

<script setup>
import { getOAuthLoginUrl } from '../config/oauth.js';

function goKakao() {
  window.location.href = getOAuthLoginUrl('kakao');
}

function goGoogle() {
  window.location.href = getOAuthLoginUrl('google');
}
</script>
