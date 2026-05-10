import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';

const STORAGE_KEY = 'total_study_data';

export const usePlansStore = defineStore('plans', () => {
  const allPlans = ref(JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}'));
  const selectedDate = ref(new Date());

  watch(
    allPlans,
    (val) => {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(val));
    },
    { deep: true }
  );

  const dateKey = computed(() => {
    const d = selectedDate.value;
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  });

  return { allPlans, selectedDate, dateKey };
});
