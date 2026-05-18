package com.soongongjandi.domain.todo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soongongjandi.domain.todo.dto.response.TodoMonthlyResponse;
import com.soongongjandi.domain.todo.service.query.TodoQueryService;
import com.soongongjandi.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/todo")
@RequiredArgsConstructor
@Tag(name = "할 일 CRUD", description = "할 일 CRUD 기능")
public class TodoV1Controller {

	private final TodoQueryService todoQueryService;

	@Operation(summary = "할 일 목록 조회", description = "할 일에 대해 월별로 조회할 수 있다.")
	@GetMapping
	public ApiResponse<List<TodoMonthlyResponse>> getTodoList(
			@AuthenticationPrincipal Long memberId,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer week,
			@RequestParam(required = false) Integer day
	) {
		List<TodoMonthlyResponse> response = todoQueryService.getTodoList(memberId, year, month, week, day);
		return ApiResponse.success(response);
	}
}
