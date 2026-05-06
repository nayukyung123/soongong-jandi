package com.soongongjandi.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 (C)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 유효하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않는 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "요청 값의 타입이 올바르지 않습니다."),

    // 인증/인가 (A)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "만료된 토큰입니다."),

    // OAuth (O)
    OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "O001", "지원하지 않는 소셜 로그인입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "O002", "소셜 로그인 인증에 실패했습니다."),

    // 유저 (U)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U002", "이미 사용 중인 닉네임입니다."),

    // 할일 (T)
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "할일을 찾을 수 없습니다."),
    TODO_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "T002", "이미 완료된 할일입니다."),

    // 세션 (S)
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "집중 세션을 찾을 수 없습니다."),
    SESSION_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "S002", "이미 진행 중인 세션이 있습니다."),

    // 커밋 & 요약 (CM/SU)
    COMMIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CM001", "마크다운 커밋 생성에 실패했습니다."),
    SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "SU001", "일별 요약을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
