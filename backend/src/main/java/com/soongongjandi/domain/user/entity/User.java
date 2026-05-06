package com.soongongjandi.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 255, nullable = false)
    private String email;

    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Gender gender;

    @Column(length = 20)
    private String provider;

    @Column(length = 255, name = "provider_id")
    private String providerId;

    @Column(length = 50)
    private String category;

    @Builder.Default
    @Column(name = "camera_interval", nullable = false)
    private int cameraInterval = 10;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Static Factory Methods
    public static User createFromOAuth(String provider, String providerId, String name, String email, Gender gender, String category) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .name(name)
                .email(email)
                .gender(gender)
                .category(category)
                .build();
    }

    public static User createLocal(String name, String email, String password, Gender gender, String category) {
        return User.builder()
                .name(name)
                .email(email)
                .password(password)
                .gender(gender)
                .category(category)
                .build();
    }

    // Domain Methods
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateCategory(String category) {
        this.category = category;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateCameraInterval(int interval) {
        this.cameraInterval = interval;
    }
}
