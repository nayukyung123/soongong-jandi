package com.soongongjandi.domain.todo.dto.response;

import java.time.LocalTime;

public record DayPlan(
        Long id,
        String title,
        String detail,
        String studyContent,
        LocalTime startAt,
        LocalTime endAt,
        int displayOrder,
        boolean completed
) {
}
