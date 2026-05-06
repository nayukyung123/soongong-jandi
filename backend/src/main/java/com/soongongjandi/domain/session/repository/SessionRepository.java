package com.soongongjandi.domain.session.repository;

import com.soongongjandi.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
