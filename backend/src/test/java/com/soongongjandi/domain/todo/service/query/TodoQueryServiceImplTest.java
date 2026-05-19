package com.soongongjandi.domain.todo.service.query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.soongongjandi.domain.studylog.entity.StudyLog;
import com.soongongjandi.domain.studylog.repository.StudyLogRepository;
import com.soongongjandi.domain.todo.dto.response.DaySummary;
import com.soongongjandi.domain.todo.dto.response.TodoDailyResponse;
import com.soongongjandi.domain.todo.dto.response.TodoWeeklyResponse;
import com.soongongjandi.domain.todo.entity.TileVariant;
import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.domain.todo.repository.TodoRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoQueryServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private StudyLogRepository studyLogRepository;

    @InjectMocks
    private TodoQueryServiceImpl todoQueryService;

    /** 단위 테스트용 Todo 빌더 — 과거 날짜 기본값. */
    private Todo buildTodo(Long id, String title, LocalDate todoDate, int displayOrder) {
        return Todo.builder()
                .id(id)
                .title(title)
                .detail("메모-" + id)
                .todoDate(todoDate)
                .displayOrder(displayOrder)
                .startAt(LocalTime.of(9, 0))
                .endAt(LocalTime.of(10, 0))
                .build();
    }

    /** 단위 테스트용 StudyLog 빌더 — endedAt이 non-null이면 완료로 간주된다. */
    private StudyLog buildStudyLog(Todo todo, LocalTime endedAt) {
        return StudyLog.builder()
                .todo(todo)
                .studyContent("공부-" + todo.getId())
                .startedAt(LocalTime.of(9, 0))
                .endedAt(endedAt)
                .build();
    }

    @Test
    @DisplayName("일간 조회 - todo가 displayOrder 순서대로 DayPlan으로 매핑된다")
    void 일간조회_displayOrder_순서대로_매핑된다() {
        LocalDate date = LocalDate.of(2020, 5, 10);
        Todo todo1 = buildTodo(1L, "할 일 1", date, 0);
        Todo todo2 = buildTodo(2L, "할 일 2", date, 1);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, date))
                .thenReturn(List.of(todo1, todo2));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of());

        TodoDailyResponse result = todoQueryService.getDaily(1L, date);

        assertThat(result.view()).isEqualTo("daily");
        assertThat(result.planDate()).isEqualTo(date);
        assertThat(result.plans()).hasSize(2);
        assertThat(result.plans().get(0).id()).isEqualTo(1L);
        assertThat(result.plans().get(0).title()).isEqualTo("할 일 1");
        assertThat(result.plans().get(0).detail()).isEqualTo("메모-1");
        assertThat(result.plans().get(1).id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("일간 조회 - StudyLog endedAt이 있으면 completed=true, studyContent가 채워진다")
    void 일간조회_endedAt이_있으면_완료로_표시된다() {
        LocalDate date = LocalDate.of(2020, 5, 10);
        Todo todo = buildTodo(1L, "할 일", date, 0);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, date))
                .thenReturn(List.of(todo));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of(buildStudyLog(todo, LocalTime.of(10, 0))));

        TodoDailyResponse result = todoQueryService.getDaily(1L, date);

        assertThat(result.plans().get(0).completed()).isTrue();
        assertThat(result.plans().get(0).studyContent()).isEqualTo("공부-1");
        assertThat(result.tileVariant()).isEqualTo(TileVariant.COMPLETE);
    }

    @Test
    @DisplayName("일간 조회 - StudyLog endedAt이 null이면 completed=false")
    void 일간조회_endedAt이_null이면_미완료다() {
        LocalDate date = LocalDate.of(2020, 5, 10);
        Todo todo = buildTodo(1L, "할 일", date, 0);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, date))
                .thenReturn(List.of(todo));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of(buildStudyLog(todo, null)));

        TodoDailyResponse result = todoQueryService.getDaily(1L, date);

        assertThat(result.plans().get(0).completed()).isFalse();
        assertThat(result.tileVariant()).isEqualTo(TileVariant.SPROUT);
    }

    @Test
    @DisplayName("일간 조회 - 계획이 없으면 빈 plans와 SOIL 타일을 반환한다")
    void 일간조회_계획이_없으면_빈_목록과_SOIL을_반환한다() {
        LocalDate date = LocalDate.of(2020, 5, 10);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, date))
                .thenReturn(List.of());

        TodoDailyResponse result = todoQueryService.getDaily(1L, date);

        assertThat(result.plans()).isEmpty();
        assertThat(result.tileVariant()).isEqualTo(TileVariant.SOIL);
    }

    @Test
    @DisplayName("일간 조회 - 미래 날짜이면 계획이 있어도 tileVariant는 SOIL이지만 plans는 그대로 반환한다")
    void 일간조회_미래날짜이면_SOIL이지만_plans는_유지된다() {
        LocalDate future = LocalDate.of(2099, 1, 1);
        Todo todo = buildTodo(1L, "미래 할 일", future, 0);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, future))
                .thenReturn(List.of(todo));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of());

        TodoDailyResponse result = todoQueryService.getDaily(1L, future);

        assertThat(result.tileVariant()).isEqualTo(TileVariant.SOIL);
        assertThat(result.plans()).hasSize(1);
    }

    @Test
    @DisplayName("일간 조회 - StudyLog가 없으면 completed=false, studyContent=null, tileVariant는 SPROUT이다")
    void 일간조회_studyLog가_없으면_미완료다() {
        LocalDate date = LocalDate.of(2020, 5, 10);
        Todo todo = buildTodo(1L, "할 일", date, 0);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, date))
                .thenReturn(List.of(todo));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of());

        TodoDailyResponse result = todoQueryService.getDaily(1L, date);

        assertThat(result.plans().get(0).completed()).isFalse();
        assertThat(result.plans().get(0).studyContent()).isNull();
        assertThat(result.tileVariant()).isEqualTo(TileVariant.SPROUT);
    }

    @Test
    @DisplayName("일간 조회 - 일부만 완료이면 tileVariant는 GRASS이다")
    void 일간조회_일부완료이면_GRASS다() {
        LocalDate date = LocalDate.of(2020, 5, 10);
        Todo todo1 = buildTodo(1L, "할 일 1", date, 0);
        Todo todo2 = buildTodo(2L, "할 일 2", date, 1);
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(1L, date))
                .thenReturn(List.of(todo1, todo2));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of(buildStudyLog(todo1, LocalTime.of(10, 0))));

        TodoDailyResponse result = todoQueryService.getDaily(1L, date);

        assertThat(result.tileVariant()).isEqualTo(TileVariant.GRASS);
    }

    @Test
    @DisplayName("주간 조회 - 월 경계를 넘는 주(2026-04-26~05-02)도 7일 전체를 반환한다")
    void 주간조회_월경계를_넘어도_7일을_반환한다() {
        LocalDate anchor = LocalDate.of(2026, 4, 28);
        LocalDate start = LocalDate.of(2026, 4, 26);
        LocalDate end = LocalDate.of(2026, 5, 2);
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(1L, start, end))
                .thenReturn(List.of());

        TodoWeeklyResponse result = todoQueryService.getWeekly(1L, anchor);

        assertThat(result.view()).isEqualTo("weekly");
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(4);
        assertThat(result.days()).hasSize(7);
        assertThat(result.days().get(0).planDate()).isEqualTo(start);
        assertThat(result.days().get(6).planDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("주간 조회 - 날짜별 planCount/completedCount와 tileVariant가 계산된다")
    void 주간조회_날짜별_집계와_타일이_계산된다() {
        LocalDate anchor = LocalDate.of(2026, 4, 28);
        LocalDate start = LocalDate.of(2026, 4, 26);
        LocalDate end = LocalDate.of(2026, 5, 2);
        LocalDate dayWithPlans = LocalDate.of(2026, 4, 28);

        Todo todo1 = buildTodo(1L, "할 일 1", dayWithPlans, 0);
        Todo todo2 = buildTodo(2L, "할 일 2", dayWithPlans, 1);
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(1L, start, end))
                .thenReturn(List.of(todo1, todo2));
        when(studyLogRepository.findByTodoIdIn(anyCollection()))
                .thenReturn(List.of(buildStudyLog(todo1, LocalTime.of(10, 0))));

        TodoWeeklyResponse result = todoQueryService.getWeekly(1L, anchor);

        DaySummary withPlans = result.days().stream()
                .filter(d -> d.planDate().equals(dayWithPlans))
                .findFirst().orElseThrow();
        assertThat(withPlans.planCount()).isEqualTo(2);
        assertThat(withPlans.completedCount()).isEqualTo(1);
        assertThat(withPlans.tileVariant()).isEqualTo(TileVariant.GRASS);

        DaySummary emptyDay = result.days().stream()
                .filter(d -> d.planDate().equals(start))
                .findFirst().orElseThrow();
        assertThat(emptyDay.planCount()).isZero();
        assertThat(emptyDay.tileVariant()).isEqualTo(TileVariant.SOIL);
    }
}
