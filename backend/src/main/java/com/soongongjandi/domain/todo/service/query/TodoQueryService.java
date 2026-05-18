package com.soongongjandi.domain.todo.service.query;

import java.util.List;

import com.soongongjandi.domain.todo.dto.response.TodoMonthlyResponse;

public interface TodoQueryService {

	List<TodoMonthlyResponse> getTodoList(Long memberId, Integer year, Integer month, Integer week, Integer day);

}
