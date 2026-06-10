package com.soongongjandi.domain.auth.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soongongjandi.domain.auth.dto.LoginRequest;
import com.soongongjandi.domain.auth.dto.TokenResponse;
import com.soongongjandi.domain.auth.oauth.OAuthClient;
import com.soongongjandi.domain.auth.oauth.OAuthUserInfo;
import com.soongongjandi.domain.member.entity.Category;
import com.soongongjandi.domain.member.entity.Member;
import com.soongongjandi.domain.member.entity.Provider;
import com.soongongjandi.domain.member.repository.MemberRepository;
import com.soongongjandi.global.common.exception.BusinessException;
import com.soongongjandi.global.common.exception.ErrorCode;
import com.soongongjandi.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final List<OAuthClient> oAuthClients;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TokenResponse loginSocial(Provider provider, String code, String redirectUri) {
        OAuthClient client = getClient(provider);
        OAuthUserInfo userInfo = client.getUserInfo(code, redirectUri);

        Member member = memberRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> registerSocialMember(userInfo));

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "비활성화된 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private Member registerSocialMember(OAuthUserInfo userInfo) {
        memberRepository.findByEmail(userInfo.getEmail()).ifPresent(m -> {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미 해당 이메일로 가입된 계정이 존재합니다.");
        });

        Member newMember = Member.builder()
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .name(userInfo.getName())
                .email(userInfo.getEmail())
                .category(Category.ETC)
                .birthDate(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .build();

        return memberRepository.save(newMember);
    }

    @Transactional
    public TokenResponse loginLocal(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (member.getPassword() == null || !passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "비활성화된 계정입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken);
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "비활성화된 계정입니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private OAuthClient getClient(Provider provider) {
        return oAuthClients.stream()
                .filter(client -> client.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED));
    }
}
