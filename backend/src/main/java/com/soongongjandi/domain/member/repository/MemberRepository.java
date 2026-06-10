package com.soongongjandi.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soongongjandi.domain.member.entity.Member;
import com.soongongjandi.domain.member.entity.Provider;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);
}
