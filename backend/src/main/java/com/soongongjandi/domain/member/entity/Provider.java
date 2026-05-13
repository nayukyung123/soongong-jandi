package com.soongongjandi.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum Provider {

	GOOGLE("구글"),
	KAKAO("카카오"),
	;

	private final String name;
}
