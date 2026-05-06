package com.soongongjandi.domain.summary.repository;

import com.soongongjandi.domain.summary.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {
}
