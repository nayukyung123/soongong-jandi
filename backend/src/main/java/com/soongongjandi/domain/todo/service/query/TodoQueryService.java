package com.soongongjandi.domain.todo.service.query;

import java.time.LocalDate;

import com.soongongjandi.domain.todo.dto.response.TodoDailyResponse;

public interface TodoQueryService {

    TodoDailyResponse getDaily(Long memberId, LocalDate date);
}
