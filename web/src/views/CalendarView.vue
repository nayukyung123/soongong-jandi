<template>
  <div class="flex-1 p-8 bg-[#F8F9FA] overflow-y-auto h-full">
    <header class="mb-6 flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-black flex items-center gap-2">
          📅 {{ currentYear }}년 {{ currentMonth }}월 공부 계획
        </h2>
        <p class="text-gray-400 text-sm font-bold mt-1">날짜를 고르고 그날의 공부 항목을 적어두세요.</p>
      </div>
      <div class="flex gap-2">
        <button @click="changeMonth(-1)" class="px-4 py-2 bg-white border rounded-xl font-bold hover:bg-gray-100 text-sm">〈 {{ currentMonth - 1 < 1 ? 12 : currentMonth - 1 }}월</button>
        <button @click="changeMonth(1)" class="px-4 py-2 bg-white border rounded-xl font-bold hover:bg-gray-100 text-sm">{{ currentMonth + 1 > 12 ? 1 : currentMonth + 1 }}월 〉</button>
      </div>
    </header>

    <!-- 캘린더 그리드 -->
    <div class="bg-white rounded-3xl border border-gray-100 overflow-hidden mb-6 shadow-sm">
      <div class="grid grid-cols-7">
        <div
          v-for="day in ['일', '월', '화', '수', '목', '금', '토']"
          :key="day"
          class="p-3 text-center text-xs font-black text-gray-400 border-b border-gray-100"
        >{{ day }}</div>
      </div>
      <div class="grid grid-cols-7">
        <div v-for="empty in currentCalendar.startDay" :key="'e-' + empty" class="h-24 border-b border-r border-gray-50" />
        <div
          v-for="date in currentCalendar.lastDate"
          :key="date"
          @click="selectDate(date)"
          :class="[
            'h-24 p-2 cursor-pointer transition border-b border-r border-gray-50 relative',
            isSameDay(date) ? 'bg-purple-50 ring-2 ring-inset ring-purple-400' : 'hover:bg-purple-50'
          ]"
        >
          <span :class="['text-xs font-black', isSameDay(date) ? 'text-purple-600' : 'text-gray-400']">{{ date }}</span>
          <div v-if="getDailyPlans(date).length > 0" class="mt-1">
            <div class="bg-purple-100 text-purple-600 text-[10px] px-2 py-1 rounded-lg font-bold text-center">
              🍇 {{ getDailyPlans(date).length }}개
            </div>
            <div class="text-[9px] text-purple-400 font-bold text-center mt-1">
              {{ getTotalTime(date) }}분
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 선택된 날짜 계획 -->
    <div class="bg-white rounded-3xl border border-gray-100 p-6 shadow-sm">
      <div class="flex justify-between items-center mb-5">
        <h3 class="text-lg font-black">{{ currentMonth }}월 {{ selectedDateNum }}일의 계획</h3>
        <span class="text-sm text-gray-400 font-bold">
          합계: {{ totalMinutes }}분 · 총 {{ dailyPlans.length }}개
        </span>
      </div>

      <!-- 계획 목록 -->
      <div class="space-y-3 mb-4">
        <div
          v-for="(plan, index) in dailyPlans"
          :key="plan.id"
          class="flex items-center gap-3 p-4 border-2 border-gray-50 rounded-2xl hover:border-purple-100 transition group"
        >
          <span class="text-gray-300 cursor-grab text-lg">≡</span>
          <input type="checkbox" v-model="plan.completed" class="w-5 h-5 accent-purple-600 cursor-pointer" />
          <span class="flex-1 font-bold text-sm" :class="plan.completed ? 'line-through text-gray-300' : 'text-gray-700'">
            {{ plan.title }}
          </span>
          <!-- 시간 입력 -->
          <div class="flex items-center gap-1 bg-purple-50 rounded-xl px-3 py-1">
            <input
              type="number"
              v-model.number="plan.minutes"
              min="0" max="480" step="10"
              class="w-12 bg-transparent text-center text-sm font-black text-purple-600 outline-none"
              @click.stop
            />
            <span class="text-xs text-purple-400 font-bold">분</span>
          </div>
          <button @click="removePlan(index)" class="text-gray-300 hover:text-red-400 text-sm opacity-0 group-hover:opacity-100 transition">✕</button>
        </div>
      </div>

      <!-- 빈 상태 -->
      <div v-if="dailyPlans.length === 0" class="py-10 text-center border-2 border-dashed border-gray-100 rounded-2xl">
        <p class="text-gray-400 font-bold text-sm">계획을 추가해보세요!</p>
      </div>

      <!-- 입력창 -->
      <div class="mt-4 p-3 border-2 border-dashed border-gray-200 rounded-2xl flex gap-3 bg-gray-50">
        <input
          v-model="newPlanTitle"
          @keyup.enter="addPlan"
          type="text"
          placeholder="+ 새 계획..."
          class="flex-1 bg-transparent focus:outline-none font-bold text-sm placeholder:text-gray-300"
        />
        <div class="flex items-center gap-1 border-l border-gray-200 pl-3">
          <input
            v-model.number="newPlanMinutes"
            type="number"
            min="0" max="480" step="10"
            class="w-14 text-center text-sm font-black text-purple-600 outline-none bg-transparent"
            placeholder="60"
          />
          <span class="text-xs text-gray-400">분</span>
        </div>
        <button @click="addPlan" class="text-purple-600 font-black px-3 hover:scale-105 transition text-sm">+ 추가</button>
      </div>

      <!-- 이동 버튼 -->
      <button
        @click="$emit('change-page', 'planner')"
        class="w-full mt-6 py-4 bg-purple-600 text-white rounded-2xl font-black text-base shadow-lg hover:bg-purple-700 transition"
      >
        다음 → 공부할 항목 선택
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps(['allPlans', 'selectedDate']);
const emit = defineEmits(['update:allPlans', 'update:selectedDate', 'change-page']);

const currentYear = ref(props.selectedDate.getFullYear());
const currentMonth = ref(props.selectedDate.getMonth() + 1);
const selectedDateNum = computed(() => props.selectedDate.getDate());
const newPlanTitle = ref('');
const newPlanMinutes = ref(60);

const getDateKey = (date) =>
  `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-${String(date).padStart(2, '0')}`;

const dailyPlans = computed({
  get: () => props.allPlans[getDateKey(selectedDateNum.value)] || [],
  set: (val) => emit('update:allPlans', { ...props.allPlans, [getDateKey(selectedDateNum.value)]: val })
});

const getDailyPlans = (date) => props.allPlans[getDateKey(date)] || [];

const getTotalTime = (date) =>
  getDailyPlans(date).reduce((sum, p) => sum + (p.minutes || 0), 0);

const totalMinutes = computed(() =>
  dailyPlans.value.reduce((sum, p) => sum + (p.minutes || 0), 0)
);

const currentCalendar = computed(() => ({
  startDay: new Date(currentYear.value, currentMonth.value - 1, 1).getDay(),
  lastDate: new Date(currentYear.value, currentMonth.value, 0).getDate()
}));

const selectDate = (date) => {
  emit('update:selectedDate', new Date(currentYear.value, currentMonth.value - 1, date));
};

const isSameDay = (date) =>
  props.selectedDate.getDate() === date && props.selectedDate.getMonth() + 1 === currentMonth.value;

const changeMonth = (delta) => {
  currentMonth.value += delta;
  if (currentMonth.value > 12) { currentMonth.value = 1; currentYear.value++; }
  else if (currentMonth.value < 1) { currentMonth.value = 12; currentYear.value--; }
};

const addPlan = () => {
  if (!newPlanTitle.value.trim()) return;
  dailyPlans.value = [
    ...dailyPlans.value,
    { id: Date.now(), title: newPlanTitle.value, completed: false, minutes: newPlanMinutes.value || 60 }
  ];
  newPlanTitle.value = '';
  newPlanMinutes.value = 60;
};

const removePlan = (index) => {
  const list = [...dailyPlans.value];
  list.splice(index, 1);
  dailyPlans.value = list;
};
</script>