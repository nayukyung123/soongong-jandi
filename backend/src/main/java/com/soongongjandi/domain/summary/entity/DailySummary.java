package com.soongongjandi.domain.summary.entity;

import com.soongongjandi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_summary", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "total_sec", nullable = false)
    private int totalSec = 0;

    @Builder.Default
    @Column(name = "session_count", nullable = false)
    private int sessionCount = 0;

    @Column(name = "avg_focus_rate", precision = 5, scale = 2)
    private BigDecimal avgFocusRate;

    @Column(nullable = false)
    private LocalDate date;

    public static DailySummary of(User user, LocalDate date, int totalSec, int sessionCount, BigDecimal avgFocusRate) {
        return DailySummary.builder()
                .user(user)
                .date(date)
                .totalSec(totalSec)
                .sessionCount(sessionCount)
                .avgFocusRate(avgFocusRate)
                .build();
    }

    public void update(int totalSec, int sessionCount, BigDecimal avgFocusRate) {
        this.totalSec = totalSec;
        this.sessionCount = sessionCount;
        this.avgFocusRate = avgFocusRate;
    }
}
