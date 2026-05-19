package com.soongongjandi.domain.todo.dto.response;

import java.time.LocalDate;

import com.soongongjandi.domain.todo.entity.Todo;

public record TodoResponse(
	Long id,
	String title,
	LocalDate todoDate
) {

	public static TodoResponse from(Todo todo) {
		return new TodoResponse(
			todo.getId(),
			todo.getTitle(),
			todo.getTodoDate()
		);
	}
}
