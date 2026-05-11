<template>
  <aside class="w-56 shrink-0 flex h-full flex-col border-r border-gray-100 bg-white px-4 py-6">
    <!-- 로고 -->
    <div class="mb-8 flex items-center gap-2 px-2">
      <span class="text-3xl leading-none" aria-hidden="true">🍇</span>
      <div>
        <h1 class="text-base font-black leading-tight text-gray-800">포도공부</h1>
        <p class="text-[10px] font-bold text-brand-400">{{ profile.name }} · Lv.{{ profile.level }}</p>
      </div>
    </div>

    <!-- 메뉴 (오늘 플래너/planner 탭 없음 — 일별 계획은 달력에서 모달로 열기) -->
    <nav class="flex-1 space-y-1">
      <button
        v-for="menu in menuItems"
        :key="menu.id"
        type="button"
        @click="$emit('change-page', menu.id)"
        :class="[
          'flex w-full items-center gap-3 rounded-2xl px-4 py-3 text-sm font-bold transition-all duration-200',
          activePage === menu.id
            ? 'bg-brand-100 text-brand-700'
            : 'text-gray-400 hover:bg-gray-50 hover:text-gray-700'
        ]"
      >
        <img
          v-if="menu.iconImg"
          class="shrink-0 object-contain h-[clamp(1.25rem,5vw,1.75rem)] w-[clamp(1.25rem,5vw,1.75rem)]"
          :src="menu.iconImg"
          alt=""
          width="24"
          height="24"
        />
        <span v-else class="text-base">{{ menu.icon }}</span>
        <span>{{ menu.name }}</span>
      </button>
    </nav>

    <div class="mt-auto pt-6 border-t border-gray-100 space-y-3">
      <SocialLoginButtons v-if="!isAuthenticated" />
      <button
        v-else
        type="button"
        class="w-full rounded-2xl py-2.5 text-xs font-black text-gray-500 hover:bg-gray-50 hover:text-gray-700 transition"
        @click="logout"
      >
        로그아웃
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import SocialLoginButtons from './SocialLoginButtons.vue';
import { useAuthStore } from '../stores/auth.js';
import { decodeJwtPayload } from '../utils/jwtPayload.js';

defineProps({ activePage: String });
defineEmits(['change-page']);

const authStore = useAuthStore();
const { isAuthenticated } = storeToRefs(authStore);

const profile = computed(() => {
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

/** 네비 소스 — planner(오늘 플래너)는 사이드바에 두지 않음 */
const CALENDAR_ICON_IMG = '/images/calendar/grape-mascot.png';
const STATS_ICON_IMG = '/images/calendar/grape-mascot-stats.png';

const NAV_SOURCE = [
  { id: 'calendar', name: '달력', iconImg: CALENDAR_ICON_IMG },
  { id: 'stats', name: '포도밭 통계', iconImg: STATS_ICON_IMG },
  { id: 'record', name: '공부 기록', icon: '≡' },
  { id: 'settings', name: '설정', icon: '⚙️' }
];

const menuItems = computed(() =>
  NAV_SOURCE.filter((item) => item.id !== 'planner' && item.name !== '오늘 플래너')
);
</script>
