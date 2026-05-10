<template>
  <aside class="w-56 bg-white border-r border-gray-100 flex flex-col py-6 px-4 h-full shrink-0">
    <!-- 로고 -->
    <div class="flex items-center gap-2 mb-8 px-2">
      <span class="text-2xl">🍇</span>
      <div>
        <h1 class="text-base font-black text-gray-800 leading-tight">포도공부</h1>
        <p class="text-[10px] text-purple-400 font-bold">{{ userInfo.name }} · Lv.{{ userInfo.level }}</p>
      </div>
    </div>

    <!-- 메뉴 -->
    <nav class="space-y-1 flex-1">
      <button
        v-for="menu in menuItems"
        :key="menu.id"
        @click="$emit('change-page', menu.id)"
        :class="[
          'w-full flex items-center gap-3 px-4 py-3 rounded-2xl font-bold transition-all duration-200 text-sm',
          activePage === menu.id
            ? 'bg-purple-100 text-purple-700'
            : 'text-gray-400 hover:bg-gray-50 hover:text-gray-700'
        ]"
      >
        <span class="text-base">{{ menu.icon }}</span>
        <span>{{ menu.name }}</span>
      </button>
    </nav>

    <!-- 오늘의 한마디 -->
    <div
      @click="updateMessage"
      class="mt-6 p-3 bg-gray-50 rounded-2xl border border-dashed border-gray-200 cursor-pointer hover:bg-purple-50 transition-colors group"
    >
      <p class="text-[10px] text-gray-400 font-bold mb-1 group-hover:text-purple-400">오늘의 한마디:</p>
      <p class="text-xs text-gray-600 font-medium leading-relaxed">"{{ userInfo.todayMessage }}"</p>
    </div>
  </aside>
</template>

<script setup>
import { ref } from 'vue';

defineProps({ activePage: String });
defineEmits(['change-page']);

const userInfo = ref({
  name: '민지',
  level: 3,
  todayMessage: '5알만 더!'
});

const menuItems = [
  { id: 'planner', name: '오늘 플래너', icon: '✏️' },
  { id: 'calendar', name: '달력', icon: '📅' },
  { id: 'stats', name: '포도밭 통계', icon: '🍇' },
  { id: 'record', name: '공부 기록', icon: '≡' },
  { id: 'settings', name: '설정', icon: '⚙️' }
];

const updateMessage = () => {
  const newMessage = prompt('오늘의 한마디를 입력하세요:', userInfo.value.todayMessage);
  if (newMessage?.trim()) userInfo.value.todayMessage = newMessage;
};
</script>