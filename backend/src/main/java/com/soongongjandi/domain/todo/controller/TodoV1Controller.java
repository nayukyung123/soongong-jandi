package com.soongongjandi.domain.todo.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soongongjandi.domain.todo.dto.response.TodoDailyResponse;
import com.soongongjandi.domain.todo.dto.response.TodoMonthlyResponse;
import com.soongongjandi.domain.todo.dto.response.TodoWeeklyResponse;
import com.soongongjandi.domain.todo.service.query.TodoQueryService;
import com.soongongjandi.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/todo")
@RequiredArgsConstructor
@Tag(name = "할 일 조회", description = "할 일 월간/주간/일간 조회")
public class TodoV1Controller {

    private final TodoQueryService todoQueryService;

    // TODO(인증): Spring Security/JWT 필터 미적용 상태. memberId는 런타임에 항상 null로
    //   주입되어 조회 결과가 0건이 된다. JWT 인증 필터 추가 시 이 주석을 제거하고
    //   인증 누락 시 401을 반환하도록 보장할 것.
    @Operation(summary = "할 일 월간 조회", description = "해당 월 각 날짜별 타일 상태와 계획 수를 조회한다.")
    @GetMapping("/monthly")
    public ApiResponse<TodoMonthlyResponse> getMonthly(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) Integer year,
            @RequestParam Integer month
    ) {
        return ApiResponse.success(todoQueryService.getMonthly(memberId, year, month));
    }

    // TODO(인증): 위와 동일 — memberId 미주입 시 0건 반환.
    @Operation(summary = "할 일 주간 조회", description = "기준 날짜가 속한 일~토 주의 각 날짜별 타일 상태를 조회한다.")
    @GetMapping("/weekly")
    public ApiResponse<TodoWeeklyResponse> getWeekly(
            @AuthenticationPrincipal Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(todoQueryService.getWeekly(memberId, date));
    }

    // TODO(인증): 위와 동일 — memberId 미주입 시 0건 반환.
    @Operation(summary = "할 일 일간 조회", description = "특정 날짜의 상세 할 일 목록을 조회한다.")
    @GetMapping("/daily")
    public ApiResponse<TodoDailyResponse> getDaily(
            @AuthenticationPrincipal Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.success(todoQueryService.getDaily(memberId, date));
    }
}
