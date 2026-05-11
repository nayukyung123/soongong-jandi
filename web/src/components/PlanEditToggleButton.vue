<template>
  <button
    type="button"
    :class="[
      'shrink-0 rounded-xl border px-3 py-1.5 text-[11px] font-black transition',
      editing
        ? 'border-brand-500 bg-white text-brand-800 shadow-sm hover:bg-brand-50'
        : 'border-brand-300 bg-[#EBE0FF] text-brand-800 shadow-sm hover:brightness-95'
    ]"
    @click="emit('click')"
  >
    {{ editing ? '완료' : '수정' }}
  </button>
</template>

<script setup>
defineProps({
  /** true일 때 완료(잠금) 모드로 표시 */
  editing: { type: Boolean, default: false }
});

const emit = defineEmits(['click']);
</script>
