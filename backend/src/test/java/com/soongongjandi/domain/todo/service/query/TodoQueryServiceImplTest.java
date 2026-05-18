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

import com.soongongjandi.domain.todo.dto.response.TodoMonthlyResponse;
import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.domain.todo.repository.TodoRepository;
import com.soongongjandi.global.common.exception.BusinessException;
import com.soongongjandi.global.common.exception.ErrorCode;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoQueryServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoQueryServiceImpl todoQueryService;

    // ───────────────────────────────────────────────────────────
    // 헬퍼
    // ───────────────────────────────────────────────────────────

    /** 예외 테스트 공통 단언 — INVALID_INPUT_VALUE BusinessException이 발생함을 검증한다 */
    private static void assertInvalidInput(ThrowingCallable callable) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
    }

    /** 단위 테스트용 가짜 Todo — startAt/endAt 포함 */
    private Todo buildTodo(Long id, String title, LocalDate todoDate) {
        return Todo.builder()
                .id(id)
                .title(title)
                .todoDate(todoDate)
                .displayOrder(1)
                .startAt(LocalTime.of(9, 0))
                .endAt(LocalTime.of(10, 0))
                .build();
    }

    // ───────────────────────────────────────────────────────────
    // 1. month만 주어지면 → 월 전체 범위 [1일, 말일]로 기간 조회
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("month만 주어지면 해당 월 1일~말일 범위로 기간 조회를 호출한다")
    void month만_주어지면_월별_기간_조회를_호출한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), any(), any()))
                .thenReturn(List.of());

        todoQueryService.getTodoList(memberId, 2026, 5, null, null);

        verify(todoRepository).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                memberId,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31));
        verify(todoRepository, never()).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(any(), any());
    }

    // ───────────────────────────────────────────────────────────
    // 2. week가 주어지면 → ISO 주 범위 [월요일, 일요일]로 기간 조회
    //    2026-05, week=1 → 2026-04-27(Mon) ~ 2026-05-03(Sun)  (인접 월 스필오버)
    //    2026-05, week=2 → 2026-05-04 ~ 2026-05-10
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("week가 주어지면 ISO 주(월~일) 범위로 기간 조회를 호출한다 (인접 월 스필오버 포함)")
    void week가_주어지면_ISO주_범위로_기간_조회를_호출한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), any(), any()))
                .thenReturn(List.of());

        // week=1: 2026-05-01(금)의 previousOrSame(MON) = 2026-04-27
        todoQueryService.getTodoList(memberId, 2026, 5, 1, null);

        verify(todoRepository).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                memberId,
                LocalDate.of(2026, 4, 27),  // 스필오버(4월)
                LocalDate.of(2026, 5, 3));
        verify(todoRepository, never()).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(any(), any());
    }

    @Test
    @DisplayName("week=2이면 2번째 ISO 주(월~일) 범위로 기간 조회를 호출한다")
    void week가_2이면_두번째_ISO주_범위로_기간_조회를_호출한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), any(), any()))
                .thenReturn(List.of());

        todoQueryService.getTodoList(memberId, 2026, 5, 2, null);

        verify(todoRepository).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                memberId,
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 5, 10));
        verify(todoRepository, never()).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(any(), any());
    }

    @Test
    @DisplayName("week=5(2026-05 최대 주차 경계)이면 5번째 ISO 주(2026-05-25~2026-05-31) 범위로 기간 조회를 호출한다")
    void week가_maxWeek_경계값이면_마지막_ISO주_범위로_기간_조회를_호출한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), any(), any()))
                .thenReturn(List.of());

        // 2026-05 maxWeek=5, week=5 → firstMonday(2026-04-27) + 4주 = 2026-05-25, end = +6일 = 2026-05-31
        todoQueryService.getTodoList(memberId, 2026, 5, 5, null);

        verify(todoRepository).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                memberId,
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 31));
        verify(todoRepository, never()).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(any(), any());
    }

    // ───────────────────────────────────────────────────────────
    // 3. day가 주어지면 → 일별 조회를 정확한 날짜로 호출
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("day가 주어지면 일별 조회를 정확한 날짜로 호출한다")
    void day가_주어지면_일별_조회를_정확한_날짜로_호출한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(memberId, LocalDate.of(2026, 5, 15)))
                .thenReturn(List.of());

        todoQueryService.getTodoList(memberId, 2026, 5, null, 15);

        verify(todoRepository).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(memberId, LocalDate.of(2026, 5, 15));
        verify(todoRepository, never()).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                any(), any(), any());
    }

    // ───────────────────────────────────────────────────────────
    // 4. day와 week가 둘 다 주어지면 → day 우선 (일별 조회만 호출)
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("day와 week가 둘 다 주어지면 day 우선으로 일별 조회를 호출하고 기간 조회는 호출하지 않는다")
    void day와_week가_둘_다_주어지면_day_우선으로_일별_조회를_호출한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateOrderByDisplayOrderAsc(memberId, LocalDate.of(2026, 5, 10)))
                .thenReturn(List.of());

        todoQueryService.getTodoList(memberId, 2026, 5, 2, 10);

        verify(todoRepository).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(memberId, LocalDate.of(2026, 5, 10));
        verify(todoRepository, never()).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                any(), any(), any());
    }

    // ───────────────────────────────────────────────────────────
    // 5. year가 null이면 → 현재 연도로 해석
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("year가 null이면 현재 연도로 해석하여 조회를 호출한다")
    void year가_null이면_현재_연도로_해석한다() {
        Long memberId = 1L;
        // 프로덕션 코드의 연도 해석 로직을 그대로 반영하며, 실행 연도에 무관하게 동작하도록 런타임에 결정한다.
        int currentYear = LocalDate.now().getYear();
        LocalDate expectedStart = LocalDate.of(currentYear, 5, 1);
        LocalDate expectedEnd = LocalDate.of(currentYear, 5, 31);

        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), eq(expectedStart), eq(expectedEnd)))
                .thenReturn(List.of());

        todoQueryService.getTodoList(memberId, null, 5, null, null);

        verify(todoRepository).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                memberId, expectedStart, expectedEnd);
    }

    // ───────────────────────────────────────────────────────────
    // 6. year가 주어지면 → 해당 연도로 해석
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("명시적으로 과거 연도(2024)가 주어지면 현재 연도로 치환되지 않고 해당 연도로 조회를 호출한다")
    void year가_주어지면_해당_연도로_해석한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), any(), any()))
                .thenReturn(List.of());

        todoQueryService.getTodoList(memberId, 2024, 3, null, null);

        verify(todoRepository).findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                memberId,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 31));
    }

    // ───────────────────────────────────────────────────────────
    // 7. month가 null이면 → BusinessException(INVALID_INPUT_VALUE)
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("month가 null이면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void month가_null이면_BusinessException을_던진다() {
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, null, null, null));
    }

    // ───────────────────────────────────────────────────────────
    // 8. month가 1~12 범위 밖이면 → BusinessException(INVALID_INPUT_VALUE)
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("month가 0이면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void month가_0이면_BusinessException을_던진다() {
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, 0, null, null));
    }

    @Test
    @DisplayName("month가 13이면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void month가_13이면_BusinessException을_던진다() {
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, 13, null, null));
    }

    // ───────────────────────────────────────────────────────────
    // 9. day가 해당 월 범위를 벗어나면 → BusinessException(INVALID_INPUT_VALUE)
    //    2026-02: lengthOfMonth=28 → day=29 는 범위 초과
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("day가 0이면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void day가_0이면_BusinessException을_던진다() {
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, 5, null, 0));
    }

    @Test
    @DisplayName("day가 해당 월 말일을 초과하면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void day가_해당월_범위를_벗어나면_BusinessException을_던진다() {
        // 2026-02는 28일까지 (평년)
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, 2, null, 29));
    }

    // ───────────────────────────────────────────────────────────
    // 10. week가 해당 월 범위를 벗어나면 → BusinessException(INVALID_INPUT_VALUE)
    //     2026-05 maxWeek=5 → week=6 은 범위 초과
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("week가 0이면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void week가_0이면_BusinessException을_던진다() {
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, 5, 0, null));
    }

    @Test
    @DisplayName("week가 해당 월 최대 주차를 초과하면 BusinessException(INVALID_INPUT_VALUE)을 던진다")
    void week가_해당월_범위를_벗어나면_BusinessException을_던진다() {
        // 2026-05 maxWeek=5 → week=6 은 초과
        assertInvalidInput(() -> todoQueryService.getTodoList(1L, 2026, 5, 6, null));
    }

    // ───────────────────────────────────────────────────────────
    // 11. repository가 반환한 Todo 목록이 TodoMonthlyResponse로 매핑되어 반환된다
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("repository가 반환한 Todo 목록이 id/title/todoDate 값이 올바른 TodoMonthlyResponse 목록으로 매핑된다")
    void repository_반환_Todo가_TodoMonthlyResponse로_매핑된다() {
        Long memberId = 1L;
        LocalDate date1 = LocalDate.of(2026, 5, 5);
        LocalDate date2 = LocalDate.of(2026, 5, 10);

        Todo todo1 = buildTodo(101L, "할 일 A", date1);
        Todo todo2 = buildTodo(102L, "할 일 B", date2);

        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), eq(LocalDate.of(2026, 5, 1)), eq(LocalDate.of(2026, 5, 31))))
                .thenReturn(List.of(todo1, todo2));

        List<TodoMonthlyResponse> result = todoQueryService.getTodoList(memberId, 2026, 5, null, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(101L);
        assertThat(result.get(0).title()).isEqualTo("할 일 A");
        assertThat(result.get(0).todoDate()).isEqualTo(date1);
        assertThat(result.get(1).id()).isEqualTo(102L);
        assertThat(result.get(1).title()).isEqualTo("할 일 B");
        assertThat(result.get(1).todoDate()).isEqualTo(date2);
    }

    // ───────────────────────────────────────────────────────────
    // 12. repository 결과가 비어있으면 → 빈 리스트 반환
    // ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("repository 결과가 비어있으면 빈 리스트를 반환한다")
    void repository_결과가_비어있으면_빈_리스트를_반환한다() {
        Long memberId = 1L;
        when(todoRepository.findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(
                eq(memberId), any(), any()))
                .thenReturn(List.of());

        List<TodoMonthlyResponse> result = todoQueryService.getTodoList(memberId, 2026, 5, null, null);

        assertThat(result).isEmpty();
        verify(todoRepository, never()).findByMemberIdAndTodoDateOrderByDisplayOrderAsc(any(), any());
    }
}
