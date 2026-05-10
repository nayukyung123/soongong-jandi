<template>
  <div class="flex-1 p-8 bg-app-canvas overflow-y-auto h-full">
    <header class="mb-6 flex flex-col gap-4 sm:flex-row sm:justify-between sm:items-center">
      <div>
        <h2 class="text-2xl font-black flex items-center gap-2">
          📅 {{ currentYear }}년 {{ currentMonth }}월
        </h2>
        <p class="text-gray-400 text-sm font-bold mt-1">
          달력에서 날짜 칸을 누르면 그날 계획을 작성할 플래너가 열려요.
        </p>
      </div>
      <div class="flex flex-wrap gap-2">
        <button
          type="button"
          @click="changeMonth(-1)"
          class="px-4 py-2 bg-white border border-gray-200 rounded-xl font-bold hover:bg-gray-100 text-sm"
        >
          〈 {{ currentMonth - 1 < 1 ? 12 : currentMonth - 1 }}월
        </button>
        <button
          type="button"
          @click="changeMonth(1)"
          class="px-4 py-2 bg-white border border-gray-200 rounded-xl font-bold hover:bg-gray-100 text-sm"
        >
          {{ currentMonth + 1 > 12 ? 1 : currentMonth + 1 }}월 〉
        </button>
      </div>
    </header>

    <!-- 캘린더 그리드 -->
    <div class="overflow-hidden rounded-3xl border border-gray-100 bg-white shadow-sm">
      <div class="grid grid-cols-7">
        <div
          v-for="day in ['일', '월', '화', '수', '목', '금', '토']"
          :key="day"
          class="p-3 text-center text-xs font-black text-gray-400 border-b border-gray-100"
        >
          {{ day }}
        </div>
      </div>
      <div class="grid grid-cols-7">
        <div
          v-for="empty in currentCalendar.startDay"
          :key="'e-' + empty"
          class="h-24 border-b border-r border-gray-50"
        />
        <div
          v-for="date in currentCalendar.lastDate"
          :key="date"
          @click="selectDate(date)"
          :class="[
            'h-24 p-2 cursor-pointer transition border-b border-r border-gray-50 relative',
            isSameDay(date) ? 'bg-brand-50 ring-2 ring-inset ring-brand-400' : 'hover:bg-brand-50'
          ]"
        >
          <span :class="['text-xs font-black', isSameDay(date) ? 'text-brand-600' : 'text-gray-400']">{{ date }}</span>
          <div v-if="getDailyPlans(date).length > 0" class="mt-1">
            <div class="bg-brand-100 text-brand-600 text-[10px] px-2 py-1 rounded-lg font-bold text-center">
              🍇 {{ getDailyPlans(date).length }}개
            </div>
            <div class="text-[9px] text-brand-400 font-bold text-center mt-1">
              {{ getTotalTime(date) }}분
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps(['allPlans', 'selectedDate']);
const emit = defineEmits(['update:selectedDate', 'open-planner']);

const currentYear = ref(props.selectedDate.getFullYear());
const currentMonth = ref(props.selectedDate.getMonth() + 1);

const getDateKey = (date) =>
  `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(date).padStart(2, '0')}`;

const getDailyPlans = (date) => props.allPlans[getDateKey(date)] || [];

const getTotalTime = (date) =>
  getDailyPlans(date).reduce((sum, p) => sum + (p.minutes || 0), 0);

const currentCalendar = computed(() => ({
  startDay: new Date(currentYear.value, currentMonth.value - 1, 1).getDay(),
  lastDate: new Date(currentYear.value, currentMonth.value, 0).getDate()
}));

const selectDate = (date) => {
  emit('update:selectedDate', new Date(currentYear.value, currentMonth.value - 1, date));
  emit('open-planner');
};

const isSameDay = (date) =>
  props.selectedDate.getFullYear() === currentYear.value &&
  props.selectedDate.getDate() === date &&
  props.selectedDate.getMonth() + 1 === currentMonth.value;

const changeMonth = (delta) => {
  currentMonth.value += delta;
  if (currentMonth.value > 12) {
    currentMonth.value = 1;
    currentYear.value++;
  } else if (currentMonth.value < 1) {
    currentMonth.value = 12;
    currentYear.value--;
  }
};
</script>
