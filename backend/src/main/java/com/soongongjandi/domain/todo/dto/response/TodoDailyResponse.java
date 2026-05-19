package com.soongongjandi.domain.todo.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.soongongjandi.domain.todo.entity.TileVariant;

public record TodoDailyResponse(
        String view,
        LocalDate planDate,
        TileVariant tileVariant,
        List<DayPlan> plans
) {
}
