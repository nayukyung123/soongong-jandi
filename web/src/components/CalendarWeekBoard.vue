<template>
  <div class="calendar-board">
    <div class="calendar-unified calendar-unified--week" :style="{ '--calendar-rows': 1 }">
      <div
        v-for="label in weekdayLabels"
        :key="'w-' + label"
        class="calendar-weekday"
      >
        <span class="calendar-weekday__label">{{ label }}</span>
      </div>
      <div class="calendar-weekday-vine" aria-hidden="true" />
      <button
        v-for="offset in weekOffsets"
        :key="'d-' + offset"
        type="button"
        class="calendar-cell calendar-cell--week-row"
        :class="cellClasses(offset)"
        @click="selectOffset(offset)"
      >
        <span class="calendar-cell__day">{{ dayInMonth[offset] }}</span>
        <span v-if="planCounts[offset] > 0" class="calendar-cell__badge">
          🍇 {{ planCounts[offset] }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import {
  getPlansForDate,
  startOfDay,
  startOfWeekSunday,
  tileVariantForDate
} from '../composables/useCalendarPlanHelpers.js';
import '../styles/calendar.css';

const props = defineProps({
  /** 해당 주를 포함하는 아무 날짜 */
  anchorDate: { type: Date, required: true },
  allPlans: { type: Object, default: () => ({}) },
  selectedDate: { type: Date, required: true }
});

const emit = defineEmits(['update:selectedDate', 'open-planner']);

const weekdayLabels = ['일', '월', '화', '수', '목', '금', '토'];
const weekOffsets = [0, 1, 2, 3, 4, 5, 6];

const weekStart = computed(() => startOfWeekSunday(props.anchorDate));

const weekDates = computed(() => {
  const out = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart.value);
    d.setDate(d.getDate() + i);
    out.push(startOfDay(d));
  }
  return out;
});

const dayInMonth = computed(() => weekDates.value.map((d) => d.getDate()));

const planCounts = computed(() => weekDates.value.map((d) => getPlansForDate(props.allPlans, d).length));

function cellClasses(offset) {
  const cellDate = weekDates.value[offset];
  const v = tileVariantForDate(props.allPlans, cellDate);
  return {
    'calendar-cell--soil': v === 'soil',
    'calendar-cell--sprout': v === 'sprout',
    'calendar-cell--grass': v === 'grass',
    'calendar-cell--complete': v === 'complete',
    'calendar-cell--selected': isSameDay(cellDate)
  };
}

function isSameDay(d) {
  return (
    props.selectedDate.getFullYear() === d.getFullYear() &&
    props.selectedDate.getMonth() === d.getMonth() &&
    props.selectedDate.getDate() === d.getDate()
  );
}

function selectOffset(offset) {
  const d = weekDates.value[offset];
  emit('update:selectedDate', new Date(d));
  emit('open-planner');
}
</script>
