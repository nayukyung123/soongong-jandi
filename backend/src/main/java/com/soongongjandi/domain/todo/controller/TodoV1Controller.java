package com.soongongjandi.domain.todo.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soongongjandi.domain.todo.dto.response.TodoResponse;
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
	public ApiResponse<List<TodoResponse>> getTodoList(
			// TODO(인증): Spring Security/JWT 필터 미적용 상태. 현재 memberId는 런타임에 항상 null로
			//   주입되며, 그 결과 조회 쿼리가 'member_id = null'이 되어 0건(빈 리스트, HTTP 200)을
			//   반환한다. 인증되지 않은 호출과 데이터 없음이 구분되지 않으므로, JWT 인증 필터가
			//   추가되면 이 주석을 제거하고 인증 누락 시 401을 반환하도록 보장할 것.
			@AuthenticationPrincipal Long memberId,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer week,
			@RequestParam(required = false) Integer day
	) {
		List<TodoResponse> response = todoQueryService.getTodoList(memberId, year, month, week, day);
		return ApiResponse.success(response);
	}
}
