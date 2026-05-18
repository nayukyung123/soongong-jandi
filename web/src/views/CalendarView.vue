<template>
  <div class="calendar-page">
    <header class="calendar-header">
      <h2 class="calendar-title">
        <img
          class="calendar-title__icon"
          src="/images/calendar/grape-mascot.png"
          alt=""
          width="36"
          height="36"
        />
        {{ rangeTitle }}
      </h2>
      <div class="calendar-header-actions">
        <div class="calendar-view-switch" role="tablist" aria-label="달력 보기 단위">
          <button
            type="button"
            role="tab"
            :aria-selected="viewMode === 'day'"
            class="calendar-view-switch__btn"
            :class="{ 'is-active': viewMode === 'day' }"
            @click="viewMode = 'day'"
          >
            일간
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="viewMode === 'week'"
            class="calendar-view-switch__btn"
            :class="{ 'is-active': viewMode === 'week' }"
            @click="viewMode = 'week'"
          >
            주간
          </button>
          <button
            type="button"
            role="tab"
            :aria-selected="viewMode === 'month'"
            class="calendar-view-switch__btn"
            :class="{ 'is-active': viewMode === 'month' }"
            @click="viewMode = 'month'"
          >
            월간
          </button>
        </div>
        <div class="calendar-toolbar">
          <button type="button" class="calendar-nav-btn" @click="shiftPeriod(-1)">
            {{ navPrevLabel }}
          </button>
          <button type="button" class="calendar-nav-btn" @click="shiftPeriod(1)">
            {{ navNextLabel }}
          </button>
          <button type="button" class="calendar-nav-btn calendar-nav-btn--home" @click="emit('go-home')">
            홈으로
          </button>
        </div>
      </div>
    </header>

    <CalendarDayBoard
      v-if="viewMode === 'day'"
      :view-date="viewDate"
      :all-plans="allPlans"
      @update:all-plans="$emit('update:allPlans', $event)"
      @request-plan-compose="onDayViewOpenCompose"
      @request-plan-edit="onDayRequestPlanEdit"
    />
    <CalendarWeekBoard
      v-else-if="viewMode === 'week'"
      :anchor-date="viewDate"
      :all-plans="allPlans"
      :selected-date="selectedDate"
      @update:selected-date="$emit('update:selectedDate', $event)"
      @open-planner="$emit('open-planner')"
    />
    <CalendarBoard
      v-else
      :year="calendarYear"
      :month="calendarMonth"
      :all-plans="allPlans"
      :selected-date="selectedDate"
      @update:selected-date="onBoardSelectDate"
      @open-planner="$emit('open-planner')"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import CalendarBoard from '../components/CalendarBoard.vue';
import CalendarWeekBoard from '../components/CalendarWeekBoard.vue';
import CalendarDayBoard from '../components/CalendarDayBoard.vue';
import { startOfDay, startOfWeekSunday } from '../composables/useCalendarPlanHelpers.js';
import '../styles/calendar.css';

const props = defineProps(['allPlans', 'selectedDate']);
const emit = defineEmits([
  'update:selectedDate',
  'update:allPlans',
  'open-planner',
  'request-plan-compose',
  'request-plan-edit',
  'go-home'
]);

function onDayViewOpenCompose() {
  emit('update:selectedDate', viewDate.value);
  emit('request-plan-compose');
}

/** 일간 「수정」→ 플래너 모달 날짜가 현재 일간과 일치하도록 */
function onDayRequestPlanEdit(planId) {
  emit('update:selectedDate', viewDate.value);
  emit('request-plan-edit', planId);
}

/** @type {import('vue').Ref<'day' | 'week' | 'month'>} */
const viewMode = ref('month');

const viewDate = ref(startOfDay(new Date(props.selectedDate)));

watch(
  () => props.selectedDate,
  (d) => {
    viewDate.value = startOfDay(new Date(d));
  }
);

const calendarYear = computed(() => viewDate.value.getFullYear());
const calendarMonth = computed(() => viewDate.value.getMonth() + 1);

const rangeTitle = computed(() => {
  const d = viewDate.value;
  const y = d.getFullYear();
  const m = d.getMonth() + 1;
  const day = d.getDate();
  const dow = ['일', '월', '화', '수', '목', '금', '토'][d.getDay()];
  if (viewMode.value === 'month') return `${y}년 ${m}월`;
  if (viewMode.value === 'day') return `${y}년 ${m}월 ${day}일 (${dow})`;
  const ws = startOfWeekSunday(d);
  const we = new Date(ws);
  we.setDate(we.getDate() + 6);
  const fmt = (x) =>
    `${x.getFullYear()}.${String(x.getMonth() + 1).padStart(2, '0')}.${String(x.getDate()).padStart(2, '0')}`;
  return `${fmt(ws)} – ${fmt(we)}`;
});

function shiftPeriod(delta) {
  const d = new Date(viewDate.value);
  if (viewMode.value === 'month') {
    d.setMonth(d.getMonth() + delta);
  } else if (viewMode.value === 'week') {
    d.setDate(d.getDate() + delta * 7);
  } else {
    d.setDate(d.getDate() + delta);
  }
  viewDate.value = startOfDay(d);
  if (viewMode.value === 'day') {
    emit('update:selectedDate', viewDate.value);
  }
}

const navPrevLabel = computed(() => {
  if (viewMode.value === 'month') {
    const prev = new Date(viewDate.value);
    prev.setMonth(prev.getMonth() - 1);
    return `〈 ${prev.getMonth() + 1}월`;
  }
  if (viewMode.value === 'week') return '〈 이전 주';
  return '〈 전날';
});

const navNextLabel = computed(() => {
  if (viewMode.value === 'month') {
    const next = new Date(viewDate.value);
    next.setMonth(next.getMonth() + 1);
    return `${next.getMonth() + 1}월 〉`;
  }
  if (viewMode.value === 'week') return '다음 주 〉';
  return '다음날 〉';
});

/** 월간 그리드에서 날짜 선택 시 헤더 기준일도 맞춤 */
function onBoardSelectDate(d) {
  viewDate.value = startOfDay(new Date(d));
  emit('update:selectedDate', d);
}
</script>
