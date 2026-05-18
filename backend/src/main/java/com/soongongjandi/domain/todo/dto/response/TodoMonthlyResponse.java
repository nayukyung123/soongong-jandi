package com.soongongjandi.domain.todo.dto.response;

import java.time.LocalDate;

import com.soongongjandi.domain.todo.entity.Todo;

public record TodoMonthlyResponse(
	Long id,
	String title,
	LocalDate todoDate
) {

	public static TodoMonthlyResponse from(Todo todo) {
		return new TodoMonthlyResponse(
			todo.getId(),
			todo.getTitle(),
			todo.getTodoDate()
		);
	}
}
