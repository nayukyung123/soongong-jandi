package com.soongongjandi.domain.auth.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soongongjandi.domain.auth.dto.TokenResponse;
import com.soongongjandi.domain.auth.service.AuthService;
import com.soongongjandi.domain.member.entity.Provider;
import com.soongongjandi.global.common.exception.BusinessException;
import com.soongongjandi.global.common.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "소셜 로그인 및 토큰 관리 API")
public class AuthController {

    private final AuthService authService;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${cookie.refresh-token-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${cookie.path:/api/v1/auth}")
    private String refreshTokenCookiePath;

    @Value("${cookie.domain:}")
    private String refreshTokenCookieDomain;

    @Value("${cookie.secure:false}")
    private boolean refreshTokenCookieSecure;

    @Operation(summary = "소셜 로그인 페이지 리다이렉트", description = "구글/카카오 인가 코드 요청 URL로 리다이렉트한다.")
    @GetMapping("/login/{provider}")
    public void login(
            @PathVariable Provider provider,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String redirectUri = getCallbackUri(request, provider);
        String authUrl;

        if (provider == Provider.KAKAO) {
            authUrl = String.format(
                    "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                    kakaoClientId, redirectUri);
        } else if (provider == Provider.GOOGLE) {
            authUrl = String.format(
                    "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=email%%20profile",
                    googleClientId, redirectUri);
        } else {
            throw new BusinessException(ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED);
        }

        response.sendRedirect(authUrl);
    }

    @Operation(summary = "소셜 로그인 콜백", description = "인가 코드를 받아 토큰을 발급하고 프론트엔드로 리다이렉트한다.")
    @GetMapping("/callback/{provider}")
    public void callback(
            @PathVariable Provider provider,
            @RequestParam String code,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String redirectUri = getCallbackUri(request, provider);
        TokenResponse tokenResponse = authService.loginSocial(provider, code, redirectUri);

        setRefreshTokenCookie(response, tokenResponse.getRefreshToken());

        String targetUrl = String.format("%s?accessToken=%s", frontendUrl, tokenResponse.getAccessToken());
        response.sendRedirect(targetUrl);
    }

    private String getCallbackUri(HttpServletRequest request, Provider provider) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append("/api/v1/auth/callback/").append(provider.name().toLowerCase());
        return url.toString();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(refreshTokenCookieSecure);
        cookie.setPath(refreshTokenCookiePath);
        if (refreshTokenCookieDomain != null && !refreshTokenCookieDomain.isBlank()) {
            cookie.setDomain(refreshTokenCookieDomain);
        }
        cookie.setMaxAge(1209600);
        response.addCookie(cookie);
    }
}
