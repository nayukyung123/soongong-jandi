package com.soongongjandi.domain.user.repository;

import com.soongongjandi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
