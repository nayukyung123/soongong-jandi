package com.soongongjandi.domain.todo.service.query;

import java.util.List;

import com.soongongjandi.domain.todo.dto.response.TodoResponse;

public interface TodoQueryService {

	List<TodoResponse> getTodoList(Long memberId, Integer year, Integer month, Integer week, Integer day);

}
