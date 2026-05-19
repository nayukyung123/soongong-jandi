package com.soongongjandi.domain.todo.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TileVariant {

    SOIL,
    SPROUT,
    GRASS,
    COMPLETE;

    /**
     * 타일 상태 판별.
     * 계획 0건이거나 미래 날짜이면 SOIL.
     * 그 외에는 완료 비율로 COMPLETE / SPROUT / GRASS.
     */
    public static TileVariant of(int planCount, int completedCount, boolean isFuture) {
        if (planCount == 0 || isFuture) {
            return SOIL;
        }
        if (completedCount == planCount) {
            return COMPLETE;
        }
        if (completedCount == 0) {
            return SPROUT;
        }
        return GRASS;
    }

    /** JSON 직렬화 시 소문자 문자열로 표현한다 (예: "grass"). */
    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
