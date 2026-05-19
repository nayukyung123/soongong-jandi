package com.soongongjandi.domain.todo.service.query;

import java.time.LocalDate;

import com.soongongjandi.domain.todo.dto.response.TodoDailyResponse;
import com.soongongjandi.domain.todo.dto.response.TodoMonthlyResponse;
import com.soongongjandi.domain.todo.dto.response.TodoWeeklyResponse;

public interface TodoQueryService {

    TodoMonthlyResponse getMonthly(Long memberId, Integer year, Integer month);

    TodoWeeklyResponse getWeekly(Long memberId, LocalDate date);

    TodoDailyResponse getDaily(Long memberId, LocalDate date);
}
