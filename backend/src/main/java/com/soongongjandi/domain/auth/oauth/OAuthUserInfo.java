package com.soongongjandi.domain.auth.oauth;

import com.soongongjandi.domain.member.entity.Provider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfo {
    private Provider provider;
    private String providerId;
    private String name;
    private String email;
}
