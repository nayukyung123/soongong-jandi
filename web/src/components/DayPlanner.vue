<template>
  <div class="min-h-0 flex flex-col gap-6 p-6 md:p-8 bg-app-canvas overflow-hidden max-h-[inherit]">

    <header class="flex flex-col gap-3 sm:flex-row sm:justify-between sm:items-end shrink-0">
      <div>
        <h2 class="text-2xl md:text-3xl font-black tracking-tight">{{ formattedDate }}의 계획</h2>
        <p class="text-gray-400 font-bold mt-1 text-sm">오늘의 포도송이를 채울 계획을 세워볼까요?</p>
      </div>
      <div class="flex gap-2">
        <button
          type="button"
          @click="moveDate(-1)"
          class="px-4 py-2 bg-white border border-gray-200 rounded-xl font-bold hover:bg-gray-100 text-sm"
        >
          〈 어제
        </button>
        <button
          type="button"
          @click="moveDate(1)"
          class="px-4 py-2 bg-white border border-gray-200 rounded-xl font-bold hover:bg-gray-100 text-sm"
        >
          내일 〉
        </button>
      </div>
    </header>

    <div class="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-6 flex-1 min-h-0">

      <div class="bg-white rounded-3xl border border-gray-100 p-6 md:p-8 flex flex-col shadow-sm overflow-hidden min-h-0">
        <h3 class="text-lg font-black mb-5 shrink-0">📝 오늘의 계획</h3>

        <div class="flex-1 overflow-y-auto space-y-3 pr-1 mb-4 min-h-[120px]">
          <div
            v-for="(plan, i) in currentPlans"
            :key="plan.id"
            class="flex items-center gap-4 p-4 border-2 border-gray-50 rounded-2xl hover:border-brand-100 transition group"
          >
            <span class="text-gray-300 text-lg cursor-grab">≡</span>
            <input type="checkbox" v-model="plan.completed" class="w-5 h-5 accent-brand-600 cursor-pointer shrink-0" />
            <span class="flex-1 font-bold text-sm" :class="plan.completed ? 'line-through text-gray-300' : 'text-gray-700'">
              {{ plan.title }}
            </span>
            <div class="flex items-center gap-1 bg-brand-50 rounded-xl px-3 py-1 shrink-0">
              <input
                type="number"
                v-model.number="plan.minutes"
                min="0"
                max="480"
                step="10"
                class="w-12 bg-transparent text-center text-sm font-black text-brand-600 outline-none"
                @click.stop
              />
              <span class="text-xs text-brand-400 font-bold">분</span>
            </div>
            <button
              type="button"
              @click="removePlan(i)"
              class="text-gray-300 hover:text-red-400 text-sm opacity-0 group-hover:opacity-100 transition"
            >
              ✕
            </button>
          </div>
        </div>

        <div v-if="currentPlans.length === 0" class="flex-1 flex items-center justify-center min-h-[100px]">
          <div class="text-center border-2 border-dashed border-gray-100 rounded-2xl p-10 w-full">
            <p class="text-gray-400 font-bold text-sm">계획을 추가해보세요!</p>
          </div>
        </div>

        <div class="border-t border-gray-100 pt-4 shrink-0">
          <div class="flex gap-3 bg-gray-50 rounded-2xl px-4 py-3 border-2 border-transparent focus-within:border-brand-200 transition">
            <input
              v-model="newTitle"
              @keyup.enter="addPlan"
              type="text"
              placeholder="+ 새 계획 추가..."
              class="flex-1 bg-transparent font-bold text-sm outline-none placeholder:text-gray-300"
            />
            <button type="button" @click="addPlan" class="text-brand-600 font-black px-3 hover:scale-105 transition text-sm shrink-0">
              추가
            </button>
          </div>
          <div class="flex justify-between items-center mt-3 px-1">
            <span class="text-xs text-gray-400 font-bold">예상 시간: {{ totalMinutes }}분 · 총 {{ currentPlans.length }}개</span>
          </div>
        </div>

        <button
          type="button"
          @click="$emit('start-study', 0)"
          :disabled="currentPlans.length === 0"
          class="mt-4 w-full py-4 bg-brand-600 text-white rounded-2xl font-black text-base shadow-lg hover:bg-brand-700 transition shrink-0 disabled:opacity-40 disabled:cursor-not-allowed"
        >
          ▶ 공부 시작 (카메라로 재기)
        </button>
      </div>

      <div class="flex flex-col gap-6 min-h-0 overflow-hidden">

        <div class="bg-white rounded-3xl border border-gray-100 p-6 shadow-sm flex flex-col items-center justify-center flex-1 min-h-[200px]">
          <p class="text-[10px] text-gray-400 font-black uppercase tracking-widest mb-4">오늘 채울 송이</p>
          <div class="flex flex-wrap justify-center gap-2 max-w-[180px] mb-4">
            <div
              v-for="i in totalGrapes"
              :key="i"
              :class="[
                'w-10 h-10 rounded-full text-xl flex items-center justify-center transition-all duration-300',
                i <= completedGrapes ? 'bg-brand-500 shadow-md' : 'bg-gray-100'
              ]"
            >
              {{ i <= completedGrapes ? '🍇' : '' }}
            </div>
          </div>
          <div class="text-2xl mb-3">🏁</div>
          <p class="text-3xl font-black text-gray-700">
            {{ completedGrapes }}
            <span class="text-lg text-gray-300 mx-1">/</span>
            <span class="text-gray-400">{{ totalGrapes }} 알</span>
          </p>
        </div>

        <div class="rounded-3xl border-2 border-stem-light/40 bg-peach/35 p-6 shadow-sm">
          <h3 class="font-black text-base text-stem mb-4">💡 작동 방식</h3>
          <ul class="space-y-3">
            <li v-for="(s, i) in steps" :key="i" class="flex gap-3 items-start">
              <span class="text-stem-light font-black text-base shrink-0">{{ i + 1 }}</span>
              <p class="text-xs font-bold text-stem/90 leading-relaxed">{{ s }}</p>
            </li>
          </ul>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps(['allPlans', 'currentDate']);
const emit = defineEmits(['update:allPlans', 'update:currentDate', 'start-study']);

const newTitle = ref('');

const dateKey = computed(() => {
  const d = props.currentDate;
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
});

const currentPlans = computed({
  get: () => props.allPlans[dateKey.value] || [],
  set: (val) => emit('update:allPlans', { ...props.allPlans, [dateKey.value]: val })
});

const formattedDate = computed(() => {
  const d = props.currentDate;
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  return `${d.getMonth() + 1}월 ${d.getDate()}일 (${days[d.getDay()]})`;
});

const moveDate = (n) => {
  const d = new Date(props.currentDate);
  d.setDate(d.getDate() + n);
  emit('update:currentDate', d);
};

const addPlan = () => {
  if (!newTitle.value.trim()) return;
  currentPlans.value = [...currentPlans.value, { id: Date.now(), title: newTitle.value, completed: false, minutes: 60 }];
  newTitle.value = '';
};

const removePlan = (i) => {
  const copy = [...currentPlans.value];
  copy.splice(i, 1);
  currentPlans.value = copy;
};

const completedCount = computed(() => currentPlans.value.filter((p) => p.completed).length);
const totalMinutes = computed(() => currentPlans.value.reduce((s, p) => s + (p.minutes || 0), 0));
const totalGrapes = computed(() => Math.max(5, currentPlans.value.length));
const completedGrapes = computed(() => completedCount.value);

const steps = [
  '계획을 적고 시작을 누르면 카메라가 켜져요.',
  '자리에 있을 때만 시간이 흘러가요.',
  '자리를 비우면 자동 일시정지됩니다.',
  '공부가 끝나면 버튼을 눌러 한 알을 달성해요!',
  '달성한 항목을 체크하면 포도밭이 늘어나요 🍇'
];
</script>
