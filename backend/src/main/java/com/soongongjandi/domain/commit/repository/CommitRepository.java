package com.soongongjandi.domain.commit.repository;

import com.soongongjandi.domain.commit.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitRepository extends JpaRepository<Commit, Long> {
}
