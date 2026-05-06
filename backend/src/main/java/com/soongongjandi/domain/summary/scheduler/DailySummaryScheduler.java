package com.soongongjandi.domain.summary.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailySummaryScheduler {

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void aggregateDailySummary() {
        // TODO: aggregate previous day from sessions/focus_logs
    }
}
