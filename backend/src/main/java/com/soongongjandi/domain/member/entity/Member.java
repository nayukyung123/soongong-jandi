package com.soongongjandi.domain.member.entity;

import java.time.LocalDate;

import com.soongongjandi.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "member", uniqueConstraints = {
	// 동시 생성 방지용 유니크 제약조건
	@UniqueConstraint(columnNames = "email"),
	@UniqueConstraint(columnNames = {"provider", "providerId"})
})
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
	@SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 50)
	@Column(name = "member_id")
	private Long id;

	@Column(length = 100, nullable = false)
	private String name;

	@Column(length = 255, nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	private Provider provider;

	private String providerId;

	private String password;

	@Builder.Default
	@Column(nullable = false)
	private boolean isActive = true;

	@Column(nullable = false)
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Category category;

	/**
	 * 팩토리 메서드
	 */
	public static Member createFromOAuth(Provider provider, String providerId, String name, String email,
		Category category) {
		return Member.builder()
			.provider(provider)
			.providerId(providerId)
			.name(name)
			.email(email)
			.category(category)
			.build();
	}

	public static Member createLocal(String name, String email, String password, Category category) {
		return Member.builder()
			.name(name)
			.email(email)
			.password(password)
			.category(category)
			.build();
	}
}
