package com.soongongjandi.global.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.soongongjandi.global.common.response.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("필수 요청 파라미터 누락 시 400(C001 INVALID_INPUT_VALUE)을 반환한다")
    void 필수_파라미터_누락_시_400을_반환한다() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("date", "LocalDate");

        ResponseEntity<ApiResponse<Void>> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getCode()).isEqualTo("C001");
        assertThat(response.getBody().getMessage()).contains("date");
    }
}
