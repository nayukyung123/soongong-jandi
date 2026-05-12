<template>
  <div class="calendar-board calendar-board--day-wrap">
    <div class="calendar-day-planner-wrap">
      <div class="calendar-day-single-bg" :class="dayBgClass">
        <div class="calendar-day-planner-layer">
          <DayPlanner
            :all-plans="allPlans"
            :current-date="dayRef"
            :embedded-surface="embeddedSurface"
            embedded-compose-via-modal
            transparent-variant
            @update:all-plans="$emit('update:allPlans', $event)"
            @request-plan-compose="$emit('request-plan-compose')"
            @request-plan-edit="$emit('request-plan-edit', $event)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import DayPlanner from './DayPlanner.vue';
import { startOfDay, tileVariantForDate } from '../composables/useCalendarPlanHelpers.js';
import '../styles/calendar.css';

const props = defineProps({
  viewDate: { type: Date, required: true },
  allPlans: { type: Object, default: () => ({}) }
});

defineEmits(['update:allPlans', 'request-plan-compose', 'request-plan-edit']);

const dayRef = computed(() => startOfDay(props.viewDate));

/** 일간 글자 대비용 — 월간 타일 종류와 동일 */
const embeddedSurface = computed(() => tileVariantForDate(props.allPlans, dayRef.value));

const dayBgClass = computed(() => {
  const v = embeddedSurface.value;
  return {
    'calendar-day-single-bg--soil': v === 'soil',
    'calendar-day-single-bg--sprout': v === 'sprout',
    'calendar-day-single-bg--grass': v === 'grass',
    'calendar-day-single-bg--complete': v === 'complete'
  };
});
</script>
