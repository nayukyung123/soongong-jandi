package com.soongongjandi.domain.auth.oauth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.soongongjandi.domain.member.entity.Provider;
import com.soongongjandi.global.common.exception.BusinessException;
import com.soongongjandi.global.common.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GoogleOAuthClient implements OAuthClient {

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public GoogleOAuthClient(
            @Value("${oauth.google.client-id}") String clientId,
            @Value("${oauth.google.client-secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }

    @Override
    public OAuthUserInfo getUserInfo(String code, String redirectUri) {
        String accessToken = getAccessToken(code, redirectUri);
        return getProfile(accessToken);
    }

    private String getAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        try {
            Map<String, Object> response = restClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response == null || !response.containsKey("access_token")) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "구글 토큰 발급 실패");
            }
            return (String) response.get("access_token");
        } catch (Exception e) {
            log.error("Failed to get Google access token", e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "구글 인증 통신 실패");
        }
    }

    private OAuthUserInfo getProfile(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response == null) {
                throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "구글 프로필 조회 실패");
            }

            String providerId = String.valueOf(response.get("id"));
            String email = (String) response.get("email");
            String name = (String) response.get("name");

            if (name == null || name.isBlank()) {
                name = "GoogleUser";
            }

            return OAuthUserInfo.builder()
                    .provider(Provider.GOOGLE)
                    .providerId(providerId)
                    .name(name)
                    .email(email)
                    .build();
        } catch (Exception e) {
            log.error("Failed to get Google profile", e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED, "구글 프로필 조회 실패");
        }
    }
}
