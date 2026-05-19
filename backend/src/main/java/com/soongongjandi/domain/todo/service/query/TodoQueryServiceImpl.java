package com.soongongjandi.domain.todo.service.query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soongongjandi.domain.studylog.entity.StudyLog;
import com.soongongjandi.domain.studylog.repository.StudyLogRepository;
import com.soongongjandi.domain.todo.dto.response.DayPlan;
import com.soongongjandi.domain.todo.dto.response.DaySummary;
import com.soongongjandi.domain.todo.dto.response.TodoDailyResponse;
import com.soongongjandi.domain.todo.dto.response.TodoWeeklyResponse;
import com.soongongjandi.domain.todo.entity.TileVariant;
import com.soongongjandi.domain.todo.entity.Todo;
import com.soongongjandi.domain.todo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoQueryServiceImpl implements TodoQueryService {

    private final TodoRepository todoRepository;
    private final StudyLogRepository studyLogRepository;

    @Override
    public TodoDailyResponse getDaily(Long memberId, LocalDate date) {
        List<Todo> todos = todoRepository
                .findByMemberIdAndTodoDateOrderByDisplayOrderAsc(memberId, date);
        Map<Long, StudyLog> studyLogByTodoId = findStudyLogs(todos);

        List<DayPlan> plans = todos.stream()
                .map(todo -> toDayPlan(todo, studyLogByTodoId.get(todo.getId())))
                .toList();

        int planCount = plans.size();
        int completedCount = (int) plans.stream().filter(DayPlan::completed).count();
        TileVariant tileVariant = TileVariant.of(planCount, completedCount, date.isAfter(LocalDate.now()));

        return new TodoDailyResponse("daily", date, tileVariant, plans);
    }

    @Override
    public TodoWeeklyResponse getWeekly(Long memberId, LocalDate date) {
        LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate end = start.plusDays(6);
        List<DaySummary> days = buildDaySummaries(memberId, start, end);
        return new TodoWeeklyResponse("weekly", date.getYear(), date.getMonthValue(), days);
    }

    /** start~end 범위의 모든 날짜에 대해 날짜별 요약을 만든다. 계획 0건 날짜도 포함한다. */
    private List<DaySummary> buildDaySummaries(Long memberId, LocalDate start, LocalDate end) {
        List<Todo> todos = todoRepository
                .findByMemberIdAndTodoDateBetweenOrderByTodoDateAscDisplayOrderAsc(memberId, start, end);
        Map<Long, StudyLog> studyLogByTodoId = findStudyLogs(todos);

        Map<LocalDate, List<Todo>> todosByDate = todos.stream()
                .collect(Collectors.groupingBy(Todo::getTodoDate));

        LocalDate today = LocalDate.now();
        List<DaySummary> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            List<Todo> dayTodos = todosByDate.getOrDefault(d, List.of());
            int planCount = dayTodos.size();
            int completedCount = (int) dayTodos.stream()
                    .filter(t -> isCompleted(studyLogByTodoId.get(t.getId())))
                    .count();
            TileVariant tileVariant = TileVariant.of(planCount, completedCount, d.isAfter(today));
            result.add(new DaySummary(d, tileVariant, planCount, completedCount));
        }
        return result;
    }

    /** todoId → StudyLog 맵. todo가 없으면 리포지토리를 호출하지 않는다. */
    private Map<Long, StudyLog> findStudyLogs(List<Todo> todos) {
        if (todos.isEmpty()) {
            return Map.of();
        }
        List<Long> todoIds = todos.stream().map(Todo::getId).toList();
        return studyLogRepository.findByTodoIdIn(todoIds).stream()
                .collect(Collectors.toMap(sl -> sl.getTodo().getId(), sl -> sl));
    }

    /** 완료 = StudyLog 존재 && endedAt non-null. */
    private boolean isCompleted(StudyLog studyLog) {
        return studyLog != null && studyLog.getEndedAt() != null;
    }

    private DayPlan toDayPlan(Todo todo, StudyLog studyLog) {
        return new DayPlan(
                todo.getId(),
                todo.getTitle(),
                todo.getDetail(),
                studyLog != null ? studyLog.getStudyContent() : null,
                todo.getStartAt(),
                todo.getEndAt(),
                todo.getDisplayOrder(),
                isCompleted(studyLog)
        );
    }
}
