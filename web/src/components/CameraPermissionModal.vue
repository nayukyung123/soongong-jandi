<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="show"
        class="camera-modal-backdrop fixed inset-y-0 right-0 left-56 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
        role="dialog"
        aria-modal="true"
        aria-labelledby="camera-modal-title"
      >
        <Transition name="modal-pop">
          <div
            v-if="show"
            class="camera-modal-card relative w-full max-w-md overflow-hidden rounded-3xl bg-[#fdf9f2] shadow-2xl ring-1 ring-black/5"
            @click.stop
          >
            <!-- 상단 장식 띠 -->
            <div class="h-1.5 w-full bg-gradient-to-r from-brand-300 via-brand-500 to-grape-deep" />
 
            <div class="px-8 py-8">
              <!-- 아이콘 -->
              <div class="mb-5 flex justify-center">
                <div class="camera-icon-wrap flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-50 shadow-inner ring-2 ring-brand-100">
                  <span class="text-3xl">📷</span>
                </div>
              </div>
 
              <!-- 제목 -->
              <h2
                id="camera-modal-title"
                class="mb-2 text-center text-2xl font-black tracking-tight text-app-ink"
              >
                카메라 접근이 필요해요
              </h2>
              <p class="mb-6 text-center text-sm font-bold leading-relaxed text-app-muted">
                실제 공부 시간만 측정하기 위해<br />
                5분에 한 번 자리에 계신지 확인해요.
              </p>
 
              <!-- 안내 뱃지 4개 -->
              <div class="mb-7 grid grid-cols-2 gap-3">
                <div
                  v-for="item in badges"
                  :key="item.label"
                  class="flex flex-col gap-1 rounded-2xl border border-brand-100 bg-white px-4 py-3 shadow-sm"
                >
                  <span class="text-xs font-black text-brand-600">✓ {{ item.label }}</span>
                  <span class="text-[11px] font-bold text-app-muted">{{ item.desc }}</span>
                </div>
              </div>
 
              <!-- 버튼 -->
              <div class="flex gap-3">
                <button
                  type="button"
                  class="flex-1 rounded-2xl border border-gray-200 bg-white px-4 py-3 text-sm font-black text-gray-500 transition hover:bg-gray-50"
                  @click="emit('later')"
                >
                  나중에
                </button>
                <button
                  type="button"
                  class="flex-[2] rounded-2xl bg-brand-600 px-4 py-3 text-sm font-black text-white shadow-md transition hover:bg-brand-700 hover:brightness-105 active:scale-[0.98]"
                  @click="handleAllow"
                >
                  카메라 허용하고 시작
                </button>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
 
<script setup>
defineProps({
  show: { type: Boolean, default: false }
});

const emit = defineEmits(['allow', 'later']);

const badges = [
  { label: '5분에 1회', desc: '스냅샷 1장' },
  { label: '기기 내 처리', desc: '서버 전송 ✕' },
  { label: '영상 저장 ✕', desc: '순간 판단만' },
  { label: '언제든 OFF', desc: '설정에서' }
];

async function handleAllow() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true });
    emit('allow', stream);
  } catch (err) {
    alert('카메라 접근에 실패했어요. 브라우저 설정을 확인해 주세요.');
  }
}
</script>
 
<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.22s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
 
.modal-pop-enter-active {
  transition: opacity 0.22s ease, transform 0.26s cubic-bezier(0.33, 1.15, 0.48, 1);
}
.modal-pop-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.modal-pop-enter-from,
.modal-pop-leave-to {
  opacity: 0;
  transform: scale(0.94) translateY(8px);
}
 
.camera-icon-wrap {
  animation: icon-bounce 0.5s cubic-bezier(0.33, 1.4, 0.48, 1) 0.1s both;
}
 
@keyframes icon-bounce {
  from { transform: scale(0.7); opacity: 0; }
  to   { transform: scale(1);   opacity: 1; }
}
</style>