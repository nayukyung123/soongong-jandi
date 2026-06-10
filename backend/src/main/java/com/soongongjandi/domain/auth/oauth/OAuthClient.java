package com.soongongjandi.domain.auth.oauth;

import com.soongongjandi.domain.member.entity.Provider;

public interface OAuthClient {
    Provider getProvider();
    OAuthUserInfo getUserInfo(String code, String redirectUri);
}
