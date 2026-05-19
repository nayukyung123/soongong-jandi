package com.soongongjandi.domain.todo.dto.response;

import java.time.LocalDate;

import com.soongongjandi.domain.todo.entity.TileVariant;

public record DaySummary(
        LocalDate planDate,
        TileVariant tileVariant,
        int planCount,
        int completedCount
) {
}
