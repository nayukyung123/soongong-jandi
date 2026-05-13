package com.soongongjandi.domain.todo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TodoStatus {
    TODO("진행 예정"),
    ING("진행 중"),
    DONE("진행 완료"),
    ;

    private final String description;
}
