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
import com.soongongjandi.domain.todo.dto.response.TodoDailyResponse;
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
}
