package com.soongongjandi.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

	TEST("수험생"),
	JOB_SEEKER("취준생"),
	DEVELOPER("개발자"),
	ETC("기타"),
	;

	private final String name;
}
