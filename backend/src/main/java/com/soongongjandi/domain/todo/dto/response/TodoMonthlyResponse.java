package com.soongongjandi.domain.todo.dto.response;

import java.util.List;

public record TodoMonthlyResponse(
        String view,
        int year,
        int month,
        List<DaySummary> days
) {
}
