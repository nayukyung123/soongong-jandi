<template>
  <div class="calendar-board">
    <div
      class="calendar-unified"
      :style="{ '--calendar-rows': calendarRowCount }"
    >
      <div
        v-for="day in ['일', '월', '화', '수', '목', '금', '토']"
        :key="'w-' + day"
        class="calendar-weekday"
      >
        <span class="calendar-weekday__label">{{ day }}</span>
      </div>
      <div class="calendar-weekday-vine" aria-hidden="true" />
      <div
        v-for="empty in currentCalendar.startDay"
        :key="'e-' + empty"
        class="calendar-pad"
        aria-hidden="true"
      />
      <button
        v-for="date in currentCalendar.lastDate"
        :key="date"
        type="button"
        class="calendar-cell"
        :class="cellClasses(date)"
        @click="selectDate(date)"
      >
        <span class="calendar-cell__day">{{ date }}</span>
          <span v-if="getDailyPlans(date).length > 0" class="calendar-cell__badge">
            🍇 {{ getDailyPlans(date).length }}
          </span>
      </button>
      <div
        v-for="n in trailingPadCount"
        :key="'trail-' + n"
        class="calendar-pad calendar-pad--trail"
        aria-hidden="true"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { tileVariantForDate, startOfDay, toDateKeyFromParts } from '../composables/useCalendarPlanHelpers.js';
import '../styles/calendar.css';

const props = defineProps({
  year: { type: Number, required: true },
  month: { type: Number, required: true },
  allPlans: { type: Object, default: () => ({}) },
  selectedDate: { type: Date, required: true }
});

const emit = defineEmits(['update:selectedDate', 'open-planner']);

const getDailyPlans = (date) => props.allPlans[toDateKeyFromParts(props.year, props.month, date)] || [];

const currentCalendar = computed(() => ({
  startDay: new Date(props.year, props.month - 1, 1).getDay(),
  lastDate: new Date(props.year, props.month, 0).getDate()
}));

const calendarRowCount = computed(() => {
  const totalCells = currentCalendar.value.startDay + currentCalendar.value.lastDate;
  return Math.ceil(totalCells / 7);
});

const trailingPadCount = computed(() => {
  const totalCells = currentCalendar.value.startDay + currentCalendar.value.lastDate;
  const slots = calendarRowCount.value * 7;
  return Math.max(0, slots - totalCells);
});

function calendarDateAt(date) {
  const d = new Date(props.year, props.month - 1, date);
  return startOfDay(d);
}

function tileVariant(date) {
  return tileVariantForDate(props.allPlans, calendarDateAt(date));
}

function cellClasses(date) {
  const v = tileVariant(date);
  return {
    'calendar-cell--soil': v === 'soil',
    'calendar-cell--grass': v === 'grass',
    'calendar-cell--complete': v === 'complete',
    'calendar-cell--selected': isSameDay(date)
  };
}

function selectDate(date) {
  emit('update:selectedDate', new Date(props.year, props.month - 1, date));
  emit('open-planner');
}

function isSameDay(date) {
  return (
    props.selectedDate.getFullYear() === props.year &&
    props.selectedDate.getDate() === date &&
    props.selectedDate.getMonth() + 1 === props.month
  );
}
</script>
