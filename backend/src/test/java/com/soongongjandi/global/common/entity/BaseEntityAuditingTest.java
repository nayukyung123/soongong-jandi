package com.soongongjandi.global.common.entity;

import com.soongongjandi.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BaseEntityAuditingTest {

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("persist 시 createdAt 과 updatedAt 이 자동으로 세팅되며 두 값이 동일하다")
    void persist_sets_createdAt_and_updatedAt() {
        AuditTestEntity entity = new AuditTestEntity("first");

        em.persist(entity);
        em.flush();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    @Test
    @DisplayName("필드 변경 후 flush 시 updatedAt 은 갱신되지만 createdAt 은 불변이다")
    void update_advances_updatedAt_but_keeps_createdAt() throws InterruptedException {
        AuditTestEntity entity = new AuditTestEntity("before");
        em.persist(entity);
        em.flush();

        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();

        Thread.sleep(10);

        entity.updateName("after");
        em.flush();

        assertThat(entity.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(entity.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("새로 persist 된 엔티티의 deletedAt 은 null 이다")
    void persist_leaves_deletedAt_null() {
        AuditTestEntity entity = new AuditTestEntity("alive");

        em.persist(entity);
        em.flush();

        assertThat(entity.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("softDelete 호출 시 deletedAt 이 현재 시각 범위로 세팅된다")
    void softDelete_sets_deletedAt() {
        AuditTestEntity entity = new AuditTestEntity("doomed");
        em.persist(entity);
        em.flush();

        LocalDateTime before = LocalDateTime.now();
        entity.softDelete();
        em.flush();
        LocalDateTime after = LocalDateTime.now();

        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getDeletedAt()).isBetween(before, after);
    }
}
