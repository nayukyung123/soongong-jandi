package com.soongongjandi.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soongongjandi.global.common.exception.BusinessException;
import com.soongongjandi.global.common.exception.ErrorCode;

class JwtTokenProviderTest {

    private final String secret = "testSecretKeyWithLengthOfAtLeast32BytesForHmacSha256EncryptionTest";
    private final long accessTokenExpire = 3600000;
    private final long refreshTokenExpire = 1209600000;

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            secret,
            accessTokenExpire,
            refreshTokenExpire
    );

    @Test
    @DisplayName("토큰 생성 및 회원 ID 추출 성공")
    void createAndGetMemberId() {
        // given
        Long memberId = 1L;

        // when
        String token = jwtTokenProvider.createAccessToken(memberId);
        Long extractedId = jwtTokenProvider.getMemberId(token);

        // then
        assertThat(token).isNotBlank();
        assertThat(extractedId).isEqualTo(memberId);
    }

    @Test
    @DisplayName("올바른 토큰 검증 성공")
    void validateToken_success() {
        // given
        String token = jwtTokenProvider.createAccessToken(1L);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 예외 발생")
    void validateToken_expired() {
        // given
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, -1000, 1209600000);
        String expiredToken = shortLivedProvider.createAccessToken(1L);

        // when & then
        assertThatThrownBy(() -> shortLivedProvider.validateToken(expiredToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_TOKEN.getMessage());
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 시 예외 발생")
    void validateToken_invalid() {
        // given
        String invalidToken = "invalidTokenString";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_TOKEN.getMessage());
    }
}
