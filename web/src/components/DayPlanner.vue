<template>
  <div
    :class="
      composeOnly
        ? 'day-planner-root--compose flex flex-col overflow-hidden bg-transparent'
        : transparentVariant
          ? 'day-planner-root--embedded flex h-full max-h-full min-h-0 flex-col overflow-x-visible overflow-y-hidden bg-transparent p-3 sm:p-4 md:p-5'
          : listOnly
          ? 'day-planner-root--modal day-planner-root--list-only flex h-full min-h-0 flex-1 flex-col overflow-hidden bg-app-canvas p-4 md:p-6'
          : 'day-planner-root--modal flex h-full min-h-0 flex-1 flex-col overflow-hidden bg-app-canvas p-6 md:p-8'
    "
  >
    <div
      :class="
        composeOnly
          ? 'flex flex-col overflow-hidden rounded-2xl bg-transparent'
          : transparentVariant
            ? 'day-planner-card--embedded flex min-h-0 flex-1 flex-col overflow-x-visible overflow-y-hidden rounded-3xl border border-white/45 bg-transparent p-4 md:p-6'
            : listOnly
              ? 'day-planner-card--modal flex h-full min-h-0 flex-1 flex-col overflow-hidden rounded-3xl border border-gray-100 bg-white p-4 shadow-sm md:p-6'
              : 'day-planner-card--modal flex h-full min-h-0 flex-1 flex-col overflow-hidden rounded-3xl border border-gray-100 bg-white p-6 shadow-sm md:p-8'
      "
    >
      <h3
        v-if="!composeOnly && !hideHeading"
        :class="[
          'mb-3 shrink-0 text-lg font-black',
          transparentVariant ? embeddedPalette.heading : 'text-app-ink drop-shadow-sm'
        ]"
      >
        {{ headingText }}
      </h3>

      <!-- 계획 작성 (embedded·모달 전체) | compose-only 모달 전용 — 일간 + 모달 추가 시 상단 작성란 숨김 -->
      <div
        v-if="showTopComposeForm"
        :class="[
          'mb-3 shrink-0 space-y-2 rounded-2xl border-2 px-3 py-3 sm:px-4 sm:py-3.5',
          transparentVariant
            ? 'border-white/55 bg-black/10 backdrop-blur-[2px] focus-within:border-brand-300/90 focus-within:ring-2 focus-within:ring-brand-200/30'
            : 'border-gray-100 bg-gray-50 focus-within:border-brand-200'
        ]"
      >
        <p
          class="text-[11px] font-black uppercase tracking-wide"
          :class="transparentVariant ? embeddedPalette.composeLabel : 'text-brand-600'"
        >
          새 계획 작성
        </p>
        <input
          v-model="newTitle"
          type="text"
          placeholder="제목 (필수)"
          :class="[
            'w-full rounded-xl border border-transparent bg-white/90 px-3 py-2 text-sm font-black outline-none focus:border-brand-300',
            transparentVariant ? embeddedPalette.newInput : 'text-gray-900 placeholder:text-gray-400'
          ]"
          @keyup.enter="addPlan"
        />
        <div
          class="flex flex-wrap items-center gap-2 sm:gap-3 text-[11px] font-bold"
          :class="transparentVariant ? embeddedPalette.scheduleMeta : 'text-gray-600'"
        >
          <span class="shrink-0 font-black">실행 예정</span>
          <label class="flex items-center gap-1.5 font-black">
            <span class="text-[10px] opacity-80">시작</span>
            <input
              v-model="newScheduleStart"
              type="time"
              :class="transparentVariant ? embeddedPalette.timeInputNew : 'rounded-lg border border-gray-200 bg-white px-2 py-1.5 text-gray-900'"
            />
          </label>
          <span class="text-gray-400">~</span>
          <label class="flex items-center gap-1.5 font-black">
            <span class="text-[10px] opacity-80">종료</span>
            <input
              v-model="newScheduleEnd"
              type="time"
              :class="transparentVariant ? embeddedPalette.timeInputNew : 'rounded-lg border border-gray-200 bg-white px-2 py-1.5 text-gray-900'"
            />
          </label>
        </div>
        <textarea
          v-model="newDetail"
          rows="3"
          placeholder="계획 내용을 작성해 주세요. (선택)"
          :class="[
            'w-full resize-y min-h-[4.5rem] max-h-40 rounded-xl border px-3 py-2 text-xs font-bold outline-none',
            transparentVariant
              ? embeddedPalette.newDetail
              : 'min-h-[4.5rem] border-gray-200 bg-white text-gray-800 placeholder:text-gray-400 focus:border-brand-300'
          ]"
        />
        <button
          type="button"
          :class="[
            'w-full rounded-2xl py-2.5 text-sm font-black transition hover:scale-[1.01]',
            transparentVariant
              ? `${embeddedPalette.addBtn} bg-brand-500/90 text-white shadow-md hover:bg-brand-600`
              : 'bg-brand-600 text-white shadow hover:bg-brand-700'
          ]"
          @click="addPlan"
        >
          계획 추가
        </button>
      </div>

      <div
        v-if="!composeOnly && !(transparentVariant && suppressPlanList)"
        :class="[
          'mb-2 flex shrink-0 px-0.5',
          listPlanReadOnly ? 'justify-end' : 'items-center justify-between',
          transparentVariant ? embeddedPalette.footerMeta : 'text-xs font-bold text-gray-500'
        ]"
      >
        <span v-if="!listPlanReadOnly">예상 시간 합계 {{ totalMinutes }}분</span>
        <span>등록 {{ currentPlans.length }}개</span>
      </div>

      <!-- 일간 embedded: 목록 스크롤 + 하단 계획 추가 -->
      <template v-if="transparentVariant && !composeOnly && !listOnly">
        <div class="flex min-h-0 flex-1 flex-col overflow-x-visible overflow-y-hidden">
        <div
          v-if="suppressPlanList"
          class="day-planner-suppress-hint flex min-h-[120px] shrink-0 flex-col justify-center"
        >
          <div
            class="flex w-full flex-col items-center justify-center rounded-2xl border-2 border-dashed border-white/55 bg-transparent px-4 py-8 text-center"
          >
            <p class="text-sm font-bold leading-relaxed" :class="embeddedPalette.emptyHint">
              <template v-if="hasPlans">
                이 날 계획 {{ currentPlans.length }}건이 있어요. 전체 목록은 계획 모달에서만 볼 수 있어요.
              </template>
              <template v-else>
                위에서 새 계획을 추가한 뒤, 목록은 계획 모달에서 확인해 주세요.
              </template>
            </p>
          </div>
        </div>
        <div
          v-else-if="hasPlans"
          class="day-planner-scroll-area day-planner-scroll-area--padded min-h-0 flex-1 space-y-3 overflow-y-auto overscroll-contain pr-1"
        >
          <div
            v-for="(plan, i) in currentPlans"
            :key="plan.id"
            class="group space-y-2 rounded-2xl border-2 border-white/60 bg-white/90 p-3 shadow-sm transition hover:border-brand-200/90 sm:p-4"
          >
            <div class="flex w-full items-start gap-2 sm:gap-3">
              <span class="shrink-0 pt-1 text-lg" :class="embeddedPalette.grab">≡</span>
              <div class="min-w-0 flex-1 space-y-2">
                <div class="flex flex-wrap items-center gap-2">
                  <input
                    v-model="plan.title"
                    type="text"
                    placeholder="제목"
                    :readonly="!isEmbeddedEditing(plan)"
                    :class="[
                      'min-w-0 flex-1 rounded-xl border px-2 py-1 text-sm font-black outline-none',
                      isEmbeddedEditing(plan)
                        ? 'border-transparent bg-transparent focus:border-brand-300/80'
                        : 'cursor-default border-transparent bg-transparent',
                      plan.completed ? 'text-gray-400 line-through' : 'text-gray-900'
                    ]"
                  />
                  <PlanEditToggleButton
                    :editing="isEmbeddedEditing(plan)"
                    @click="toggleEmbeddedEdit(plan)"
                  />
                </div>
                <div
                  class="flex flex-wrap items-center justify-between gap-2 rounded-xl bg-[#EBE0FF] px-3 py-2.5"
                >
                  <template v-if="isEmbeddedEditing(plan)">
                    <div class="flex min-w-0 flex-1 flex-wrap items-center gap-2 text-[11px] font-black text-brand-900">
                      <span class="shrink-0">실행 예정</span>
                      <input
                        v-model="plan.scheduleStart"
                        type="time"
                        class="rounded-lg border border-brand-200/80 bg-white px-2 py-1 text-xs font-black text-gray-900 outline-none focus:border-brand-400"
                      />
                      <span class="text-brand-400">~</span>
                      <input
                        v-model="plan.scheduleEnd"
                        type="time"
                        class="rounded-lg border border-brand-200/80 bg-white px-2 py-1 text-xs font-black text-gray-900 outline-none focus:border-brand-400"
                      />
                    </div>
                  </template>
                  <p
                    v-else
                    class="min-w-0 flex-1 text-[12px] font-black leading-snug text-brand-900"
                  >
                    실행 예정 {{ plan.scheduleStart || '—' }} ~ {{ plan.scheduleEnd || '—' }}
                  </p>
                </div>
                <div class="flex items-start gap-2">
                  <textarea
                    v-model="plan.detail"
                    rows="3"
                    placeholder="계획 내용"
                    :readonly="!isEmbeddedEditing(plan)"
                    :class="[
                      'min-h-[4.5rem] min-w-0 flex-1 resize-y rounded-xl border px-3 py-2 text-xs font-bold outline-none',
                      isEmbeddedEditing(plan)
                        ? 'border-gray-200/90 bg-white text-gray-800 placeholder:text-gray-400 focus:border-brand-300'
                        : 'cursor-default border-gray-100 bg-gray-50/90 text-gray-700 placeholder:text-gray-400'
                    ]"
                  />
                  <div class="flex shrink-0 flex-col items-center justify-start pt-0.5">
                    <span
                      v-if="isPlanDateToday && plan.completed"
                      class="timer-grape-done"
                      aria-label="완료된 계획"
                      title="완료된 계획"
                    >
                      <img src="/images/timer/plan-completed-grape.png" alt="" />
                    </span>
                    <button
                      v-else-if="isPlanDateToday && showStartControl(plan)"
                      type="button"
                      class="timer-grape-btn"
                      :disabled="timerBusy && activePlanId !== plan.id"
                      aria-label="시작"
                      @click="startTimerForPlan(plan.id)"
                    >
                      <img src="/images/timer/start.png" alt="" class="pointer-events-none" />
                    </button>
                  </div>
                </div>
              </div>
              <div class="flex shrink-0 flex-col items-end gap-2 pt-0.5">
                <button type="button" :class="embeddedPalette.deleteBtn" @click="removePlan(i)">
                  ✕
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="!suppressPlanList" class="day-planner-empty-area flex min-h-[120px] shrink-0 flex-col justify-center">
          <div
            class="day-planner-empty-area__inner flex w-full items-center justify-center rounded-2xl border-2 border-dashed border-white/55 bg-transparent py-8 text-center"
          >
            <p class="text-sm font-bold" :class="embeddedPalette.emptyHint">
              {{
                embeddedComposeViaModal
                  ? '아래 「계획 추가하기」를 눌러 새 계획을 추가해 보세요.'
                  : '위 입력란에 작성한 뒤 「계획 추가」를 눌러 주세요.'
              }}
            </p>
          </div>
        </div>

        <div
          v-if="embeddedComposeViaModal"
          class="shrink-0 border-t border-white/25 pt-3"
        >
          <button
            type="button"
            class="w-full rounded-2xl border-2 border-brand-500 bg-brand-600 px-4 py-3.5 text-center text-sm font-black text-white shadow-md transition hover:bg-brand-700 hover:brightness-[1.02]"
            @click="emit('request-plan-compose')"
          >
            계획 추가하기
          </button>
        </div>
        </div>
      </template>

      <!-- 플래너 모달 등: 목록만 스크롤 (작성은 별도 모달) -->
      <template v-else-if="!transparentVariant && !composeOnly">
        <div class="day-planner-modal-list-wrap day-planner-modal-list-wrap--main min-h-0 flex-1 overflow-hidden">
          <div v-if="hasPlans" class="day-planner-modal-scroll day-planner-scroll-area--padded space-y-3">
            <div
              v-for="(plan, i) in currentPlans"
              :key="plan.id"
              class="group space-y-2 rounded-2xl border border-gray-100 bg-white p-3 shadow-sm transition hover:border-brand-100 sm:p-4"
            >
              <div class="flex w-full items-start gap-2 sm:gap-3">
                <span class="shrink-0 cursor-grab pt-1 text-lg text-gray-300">≡</span>
                <div class="min-w-0 flex-1 space-y-2">
                  <template v-if="listPlanReadOnly && editingPlanId !== plan.id">
                    <div class="flex flex-wrap items-center gap-2">
                      <p
                        class="min-w-0 flex-1 text-sm font-black leading-snug text-gray-800"
                        :class="plan.completed ? 'text-gray-400 line-through' : ''"
                      >
                        {{ plan.title || '제목 없음' }}
                      </p>
                      <button
                        type="button"
                        class="shrink-0 rounded-xl border border-brand-200 bg-brand-50 px-3 py-1 text-sm font-black text-brand-700 shadow-sm transition hover:bg-brand-100"
                        @click="startEditPlan(plan)"
                      >
                        수정
                      </button>
                    </div>
                    <div class="flex items-center gap-2">
                      <div
                        class="flex min-w-0 flex-1 flex-wrap items-center gap-x-3 gap-y-1 rounded-xl border border-brand-100 bg-brand-50/70 px-3 py-2.5"
                      >
                        <span class="text-[11px] font-black text-brand-700">실행 예정</span>
                        <span class="text-sm font-black tabular-nums text-gray-900">
                          {{ plan.scheduleStart || '—' }}
                          <span class="mx-1 font-normal text-gray-400">~</span>
                          {{ plan.scheduleEnd || '—' }}
                        </span>
                      </div>
                      <span
                        v-if="isPlanDateToday && plan.completed"
                        class="timer-grape-done shrink-0"
                        aria-label="완료된 계획"
                        title="완료된 계획"
                      >
                        <img src="/images/timer/plan-completed-grape.png" alt="" />
                      </span>
                      <button
                        v-else-if="isPlanDateToday && showStartControl(plan)"
                        type="button"
                        class="timer-grape-btn shrink-0"
                        :disabled="timerBusy && activePlanId !== plan.id"
                        aria-label="시작"
                        @click="startTimerForPlan(plan.id)"
                      >
                        <img src="/images/timer/start.png" alt="" class="pointer-events-none" />
                      </button>
                    </div>
                    <p class="min-h-[2.75rem] whitespace-pre-wrap rounded-xl bg-gray-50/90 px-3 py-2 text-xs font-bold leading-relaxed text-gray-700">
                      {{ plan.detail || '내용 없음' }}
                    </p>
                  </template>
                  <template v-else>
                    <div class="flex flex-wrap items-center gap-2">
                      <input
                        v-model="plan.title"
                        type="text"
                        placeholder="제목"
                        :class="[
                          listPlanReadOnly ? 'w-full' : 'min-w-0 flex-1',
                          'rounded-xl border border-transparent bg-white px-2 py-1.5 text-sm font-black outline-none focus:border-brand-200',
                          plan.completed ? 'text-gray-300 line-through' : 'text-gray-700'
                        ]"
                      />
                      <div
                        v-if="!listPlanReadOnly"
                        class="flex shrink-0 items-center gap-1 rounded-xl bg-brand-50 px-3 py-1"
                      >
                        <input
                          type="number"
                          v-model.number="plan.minutes"
                          min="0"
                          max="480"
                          step="10"
                          class="w-12 bg-transparent text-center text-sm font-black text-brand-600 outline-none"
                          @click.stop
                        />
                        <span class="text-xs font-bold text-brand-400">분</span>
                      </div>
                    </div>
                    <div v-if="listPlanReadOnly" class="flex items-center gap-2">
                      <div class="min-w-0 flex-1 rounded-xl border border-brand-100 bg-brand-50/70 px-3 py-2.5">
                        <p class="mb-2 text-[11px] font-black text-brand-700">실행 예정</p>
                        <div class="flex flex-wrap items-center gap-3 text-[11px] font-bold text-gray-800">
                          <label class="flex items-center gap-2">
                            <span class="text-[10px] font-black text-gray-500">시작</span>
                            <input
                              v-model="plan.scheduleStart"
                              type="time"
                              class="rounded-lg border border-gray-200 bg-white px-2 py-1.5 text-sm font-black text-gray-900 outline-none focus:border-brand-400"
                            />
                          </label>
                          <span class="text-gray-300">~</span>
                          <label class="flex items-center gap-2">
                            <span class="text-[10px] font-black text-gray-500">종료</span>
                            <input
                              v-model="plan.scheduleEnd"
                              type="time"
                              class="rounded-lg border border-gray-200 bg-white px-2 py-1.5 text-sm font-black text-gray-900 outline-none focus:border-brand-400"
                            />
                          </label>
                        </div>
                      </div>
                      <span
                        v-if="isPlanDateToday && plan.completed"
                        class="timer-grape-done shrink-0 self-center"
                        aria-label="완료된 계획"
                        title="완료된 계획"
                      >
                        <img src="/images/timer/plan-completed-grape.png" alt="" />
                      </span>
                      <button
                        v-else-if="isPlanDateToday && showStartControl(plan)"
                        type="button"
                        class="timer-grape-btn shrink-0 self-center"
                        :disabled="timerBusy && activePlanId !== plan.id"
                        aria-label="시작"
                        @click="startTimerForPlan(plan.id)"
                      >
                        <img src="/images/timer/start.png" alt="" class="pointer-events-none" />
                      </button>
                    </div>
                    <textarea
                      v-model="plan.detail"
                      rows="2"
                      placeholder="계획 내용"
                      class="w-full rounded-xl border border-gray-100 bg-gray-50/90 px-3 py-2 text-xs font-bold text-gray-700 outline-none focus:border-brand-200"
                    />
                  </template>
                </div>
                <div class="flex shrink-0 flex-col items-end gap-1 self-start pt-0">
                  <template v-if="listPlanReadOnly && editingPlanId === plan.id">
                    <button
                      type="button"
                      class="rounded-xl bg-brand-600 px-2.5 py-1 text-[11px] font-black text-white shadow-sm transition hover:bg-brand-700"
                      @click="finishEditPlan"
                    >
                      완료
                    </button>
                    <button
                      type="button"
                      class="rounded-xl border border-gray-200 bg-white px-2.5 py-1 text-[11px] font-black text-gray-600 shadow-sm transition hover:bg-gray-50"
                      @click="cancelEditPlan(plan, i)"
                    >
                      취소
                    </button>
                  </template>
                  <button
                    type="button"
                    class="text-sm text-gray-300 opacity-0 transition hover:text-red-400 group-hover:opacity-100"
                    @click="removePlan(i)"
                  >
                    ✕
                  </button>
                </div>
              </div>
              <div
                v-if="!listPlanReadOnly"
                class="flex items-center justify-between gap-2 pl-7 sm:pl-8"
              >
                <p class="min-w-0 flex-1 text-[10px] font-bold text-gray-400">
                  실행 예정 {{ plan.scheduleStart || '—' }} ~ {{ plan.scheduleEnd || '—' }}
                </p>
                <span
                  v-if="isPlanDateToday && plan.completed"
                  class="timer-grape-done shrink-0"
                  aria-label="완료된 계획"
                  title="완료된 계획"
                >
                  <img src="/images/timer/plan-completed-grape.png" alt="" />
                </span>
                <button
                  v-else-if="isPlanDateToday && showStartControl(plan)"
                  type="button"
                  class="timer-grape-btn shrink-0"
                  :disabled="timerBusy && activePlanId !== plan.id"
                  aria-label="시작"
                  @click="startTimerForPlan(plan.id)"
                >
                  <img src="/images/timer/start.png" alt="" class="pointer-events-none" />
                </button>
              </div>
            </div>
          </div>
          <div v-else class="day-planner-modal-empty">
            <div
              class="flex h-full min-h-[140px] w-full items-center justify-center rounded-2xl border-2 border-dashed border-gray-100 bg-gray-50/60 px-6 py-8 text-center"
            >
              <p class="text-sm font-bold text-gray-400">
                아래 「계획 추가」버튼으로 새 계획을 추가해 보세요.
              </p>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { storeToRefs } from 'pinia';
import { usePlanTimerStore } from '../stores/planTimer.js';
import { toDateKey } from '../composables/useCalendarPlanHelpers.js';
import PlanEditToggleButton from './PlanEditToggleButton.vue';

const props = defineProps({
  allPlans: { type: Object, required: true },
  currentDate: { type: Date, required: true },
  /** 일간 달력 등 배경 타일 위에 올릴 때 카드·바깥 배경 투명 처리 */
  transparentVariant: { type: Boolean, default: false },
  /** 플래너 모달: 할 일 목록만 표시 (작성은 PlanComposeModal) */
  listOnly: { type: Boolean, default: false },
  /** 계획 작성 전용 소형 모달 */
  composeOnly: { type: Boolean, default: false },
  /** PlanComposeModal 등에서 바깥 제목과 겹치지 않게 내부 큰 제목 숨김 */
  hideHeading: { type: Boolean, default: false },
  /** 일간 배경 타일 종류 — 글자 대비용 (soil | sprout | grass | complete) */
  embeddedSurface: {
    type: String,
    default: null,
    validator: (v) => v == null || ['soil', 'sprout', 'grass', 'complete'].includes(v)
  },
  /** 일간 타일 등: 목록 카드 대신 안내만 표시(목록은 계획 모달에서만) */
  suppressPlanList: { type: Boolean, default: false },
  /** 플래너 큰 모달: 목록은 기본 조회만, 「수정」으로 행 단위 편집 */
  listPlanReadOnly: { type: Boolean, default: false },
  /** 일간 타일: 상단 인라인 작성란 대신 「계획 추가」모달로만 추가 */
  embeddedComposeViaModal: { type: Boolean, default: false },
  /** 플래너 모달에서 해당 계획 행을 즉시 편집 모드로 */
  focusEditPlanId: { type: [String, Number], default: null }
});

const emit = defineEmits([
  'update:allPlans',
  'plan-added',
  'plan-timer-started',
  'request-plan-compose',
  'request-plan-edit',
  'before-start', 
]);

/** 상단 큰 작성 블록 표시 여부 */
const showTopComposeForm = computed(
  () =>
    props.composeOnly ||
    (!props.listOnly && !(props.embeddedComposeViaModal && props.transparentVariant))
);

const planTimer = usePlanTimerStore();
const { status: timerStatus, selectedPlanId: activePlanId } = storeToRefs(planTimer);

const isPlanDateToday = computed(() => dateKey.value === toDateKey(new Date()));
const timerBusy = computed(() => timerStatus.value !== 'idle');

function startTimerForPlan(planId) {
  if (!isPlanDateToday.value) return;
  const plan = currentPlans.value.find((p) => p.id === planId);
  if (!plan || plan.completed) return;
  emit('before-start', () => {
    const ok = planTimer.startPlanImmediate(planId);
    if (ok) emit('plan-timer-started');
  });
}

/** 완료된 계획은 시작 불가. 같은 줄에서 타이머가 돌아가는 동안은 시작 버튼 숨김 */
function showStartControl(plan) {
  if (plan.completed) return false;
  if (activePlanId.value !== plan.id) return true;
  return timerStatus.value === 'idle';
}

const headingText = computed(() => {
  const d = props.currentDate;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const cur = new Date(d);
  cur.setHours(0, 0, 0, 0);
  if (cur.getTime() === today.getTime()) return '📝 오늘의 계획';
  return '📝 이 날의 계획';
});

const newTitle = ref('');
const newDetail = ref('');
const newScheduleStart = ref('09:00');
const newScheduleEnd = ref('10:00');

/** 큰 플래너 모달(listPlanReadOnly): 한 번에 한 행만 편집 */
const editingPlanId = ref(null);
const editBackup = ref(null);

/** 일간 embedded: 「수정」으로만 제목·내용·시간 입력 해제 */
const embeddedEditingPlanId = ref(null);

const dateKey = computed(() => {
  const d = props.currentDate;
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
});

const currentPlans = computed({
  get: () => props.allPlans[dateKey.value] || [],
  set: (val) => emit('update:allPlans', { ...props.allPlans, [dateKey.value]: val })
});

watch(dateKey, () => {
  editingPlanId.value = null;
  editBackup.value = null;
  embeddedEditingPlanId.value = null;
});

function isEmbeddedEditing(plan) {
  return embeddedEditingPlanId.value === plan.id;
}

function toggleEmbeddedEdit(plan) {
  if (embeddedEditingPlanId.value === plan.id) {
    embeddedEditingPlanId.value = null;
  } else {
    embeddedEditingPlanId.value = plan.id;
  }
}

function snapshotPlan(plan) {
  return {
    title: plan.title,
    detail: plan.detail,
    minutes: plan.minutes,
    scheduleStart: plan.scheduleStart,
    scheduleEnd: plan.scheduleEnd,
    completed: !!plan.completed
  };
}

function startEditPlan(plan) {
  if (editingPlanId.value != null && editingPlanId.value !== plan.id) {
    const oldIdx = currentPlans.value.findIndex((p) => p.id === editingPlanId.value);
    if (oldIdx !== -1) cancelEditPlan(currentPlans.value[oldIdx], oldIdx);
  }
  editingPlanId.value = plan.id;
  editBackup.value = snapshotPlan(plan);
}

function cancelEditPlan(plan, index) {
  if (editBackup.value) {
    const b = editBackup.value;
    const next = [...currentPlans.value];
    next[index] = { ...plan, ...b };
    currentPlans.value = next;
  }
  editingPlanId.value = null;
  editBackup.value = null;
}

function finishEditPlan() {
  editingPlanId.value = null;
  editBackup.value = null;
}

watch(
  () => props.focusEditPlanId,
  (id) => {
    if (!props.listPlanReadOnly || id == null) return;
    nextTick(() => {
      const plan = currentPlans.value.find((p) => String(p.id) === String(id));
      if (plan) startEditPlan(plan);
    });
  }
);

const addPlan = () => {
  if (!newTitle.value.trim()) return;
  currentPlans.value = [
    ...currentPlans.value,
    {
      id: Date.now(),
      title: newTitle.value.trim(),
      detail: newDetail.value.trim(),
      scheduleStart: newScheduleStart.value || '09:00',
      scheduleEnd: newScheduleEnd.value || '10:00',
      completed: false,
      minutes: 60
    }
  ];
  newTitle.value = '';
  newDetail.value = '';
  newScheduleStart.value = '09:00';
  newScheduleEnd.value = '10:00';

  if (props.composeOnly) {
    emit('plan-added');
  }
};

const removePlan = (i) => {
  const copy = [...currentPlans.value];
  const removed = copy[i];
  copy.splice(i, 1);
  currentPlans.value = copy;
  if (removed && editingPlanId.value === removed.id) {
    editingPlanId.value = null;
    editBackup.value = null;
  }
  if (removed && embeddedEditingPlanId.value === removed.id) {
    embeddedEditingPlanId.value = null;
  }
};

const totalMinutes = computed(() => currentPlans.value.reduce((s, p) => s + (p.minutes || 0), 0));

const hasPlans = computed(() => currentPlans.value.length > 0);

/** 일간: 흙밭은 밝은 글자+짙은 그림자, 잔디/완료는 진한 글자+밝은 후광 */
const embeddedPalette = computed(() => {
  const idle = {
    heading: '',
    planActive: '',
    planDone: '',
    emptyHint: '',
    footerMeta: '',
    composeLabel: '',
    newInput: '',
    newDetail: '',
    grab: '',
    minuteNum: '',
    minuteSuffix: '',
    addBtn: '',
    deleteBtn: '',
    detailArea: '',
    scheduleHint: '',
    scheduleMeta: '',
    timeInputNew: ''
  };
  if (!props.transparentVariant) return idle;

  const halo =
    '[text-shadow:0_1px_2px_rgba(255,255,255,0.98),0_0_12px_rgba(255,255,255,0.82),0_0_2px_rgba(255,255,255,0.95)]';
  const soilShadow =
    '[text-shadow:0_1px_4px_rgba(35,22,10,0.95),0_0_14px_rgba(0,0,0,0.55)]';
  const soilSoft =
    '[text-shadow:0_1px_3px_rgba(35,22,10,0.92),0_0_10px_rgba(0,0,0,0.42)]';

  const s = props.embeddedSurface ?? 'grass';

  if (s === 'soil' || s === 'sprout') {
    return {
      heading: `text-[#fff9f0] ${soilShadow}`,
      planActive: `flex-1 text-[#fffdf8] ${soilSoft}`,
      planDone: 'flex-1 text-stone-400 line-through',
      emptyHint: `text-[#fff5e8] ${soilSoft}`,
      footerMeta: `text-xs font-bold text-[#ede8e0] ${soilSoft}`,
      newInput:
        'text-[#fffdf8] placeholder:text-stone-400/90 [text-shadow:0_1px_2px_rgba(35,22,10,0.85)]',
      newDetail:
        'border border-white/40 bg-black/20 text-[#fffdf8] placeholder:text-stone-400/80 [text-shadow:0_1px_2px_rgba(35,22,10,0.85)] focus:border-brand-300/90',
      grab: 'cursor-grab text-stone-400',
      minuteNum: `w-12 bg-transparent text-center text-sm font-black text-brand-200 outline-none ${soilSoft}`,
      minuteSuffix: 'text-brand-300',
      addBtn: `text-brand-200 ${soilSoft}`,
      deleteBtn:
        'text-sm text-stone-500 opacity-0 transition hover:text-red-300 group-hover:opacity-100 [text-shadow:0_1px_2px_rgba(0,0,0,0.65)]',
      detailArea: `mt-0 w-full resize-none rounded-xl border border-white/35 bg-black/15 px-3 py-2 text-xs font-bold text-[#fffdf8] outline-none placeholder:text-stone-400/85 focus:border-brand-300/90 ${soilSoft}`,
      composeLabel: `text-brand-200 ${soilSoft}`,
      scheduleHint: `text-brand-200/90 ${soilSoft}`,
      scheduleMeta: `text-[#ede8e0] ${soilSoft}`,
      timeInputNew: `rounded-lg border border-white/45 bg-black/20 px-2 py-1.5 font-black text-brand-100 outline-none focus:border-brand-300 ${soilSoft}`
    };
  }

  /* grass · complete — 동일 계열 */
  return {
    heading: `text-gray-900 ${halo}`,
    planActive: `flex-1 text-gray-900 ${halo}`,
    planDone: 'flex-1 text-gray-600 line-through',
    emptyHint: `text-gray-900 ${halo}`,
    footerMeta: `text-xs font-bold text-gray-800 [text-shadow:0_1px_2px_rgba(255,255,255,0.9),0_0_10px_rgba(255,255,255,0.65)]`,
    newInput: 'text-gray-900 placeholder:text-gray-600',
    newDetail:
      'border border-gray-200/90 bg-white/90 text-gray-900 placeholder:text-gray-500 focus:border-brand-300',
    grab: 'cursor-grab text-gray-600',
    minuteNum: 'w-12 bg-transparent text-center text-sm font-black text-brand-700 outline-none',
    minuteSuffix: 'text-brand-500',
    addBtn: 'text-brand-600',
    deleteBtn:
      'text-sm text-gray-500 opacity-0 transition hover:text-red-500 group-hover:opacity-100',
    detailArea:
      'mt-0 w-full resize-none rounded-xl border border-gray-200/80 bg-white/70 px-3 py-2 text-xs font-bold text-gray-800 outline-none placeholder:text-gray-400 focus:border-brand-300',
    composeLabel: 'text-brand-600',
    scheduleHint: 'text-gray-600',
    scheduleMeta: 'text-gray-700',
    timeInputNew:
      'rounded-lg border border-gray-300 bg-white px-2 py-1.5 font-black text-gray-900 outline-none focus:border-brand-400'
  };
});
</script>

<style scoped>
/* 모달: 작성란은 상단 고정 — 남는 높이에서 목록만 스크롤 */
.day-planner-modal-list-wrap {
  display: flex;
  flex-direction: column;
  min-height: 0;
  border-radius: 1rem;
  border: 1px solid rgb(243 244 246);
  background: rgb(249 250 251 / 0.65);
}

/* 플래너 모달(list-only): 목록 영역이 너무 납작해지지 않도록 */
.day-planner-modal-list-wrap--main {
  min-height: min(52vh, 560px);
}

.day-planner-modal-scroll {
  box-sizing: border-box;
  flex: 1 1 0;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 0.75rem 0.75rem 1rem 1rem;
}

.day-planner-modal-empty {
  box-sizing: border-box;
  flex: 1 1 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0.75rem;
}

/* 스크롤 끝까지 내릴 때 마지막 카드가 잘리지 않도록 */
.day-planner-scroll-area--padded {
  padding-bottom: 2.75rem;
  scroll-padding-bottom: 2rem;
}
</style>
