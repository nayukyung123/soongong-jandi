<template>
  <aside class="w-56 shrink-0 flex h-full flex-col border-r border-gray-100 bg-white px-4 py-6">
    <!-- 로고 -->
    <div class="mb-8 flex items-center gap-2 px-2">
      <span class="text-2xl">🍇</span>
      <div>
        <h1 class="text-base font-black leading-tight text-gray-800">포도공부</h1>
        <p class="text-[10px] font-bold text-brand-400">{{ userInfo.name }} · Lv.{{ userInfo.level }}</p>
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
        <span class="text-base">{{ menu.icon }}</span>
        <span>{{ menu.name }}</span>
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { computed, ref } from 'vue';

defineProps({ activePage: String });
defineEmits(['change-page']);

const userInfo = ref({
  name: '민지',
  level: 3
});

/** 네비 소스 — planner(오늘 플래너)는 사이드바에 두지 않음 */
const NAV_SOURCE = [
  { id: 'calendar', name: '달력', icon: '📅' },
  { id: 'stats', name: '포도밭 통계', icon: '🍇' },
  { id: 'record', name: '공부 기록', icon: '≡' },
  { id: 'settings', name: '설정', icon: '⚙️' }
];

const menuItems = computed(() =>
  NAV_SOURCE.filter((item) => item.id !== 'planner' && item.name !== '오늘 플래너')
);
</script>
