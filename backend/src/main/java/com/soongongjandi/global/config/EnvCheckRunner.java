package com.soongongjandi.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class EnvCheckRunner implements ApplicationRunner {

    private static final List<String> REQUIRED = List.of(
            "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD",
            "JWT_SECRET"
    );

    private static final int JWT_SECRET_MIN_LENGTH = 32;

    private final Environment env;

    public EnvCheckRunner(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> missing = REQUIRED.stream()
                .filter(key -> {
                    String v = env.getProperty(key);
                    return v == null || v.isBlank();
                })
                .toList();

        if (!missing.isEmpty()) {
            String msg = """

                ╔══════════════════════════════════════════════════╗
                ║  필수 환경변수 누락                              ║
                ╠══════════════════════════════════════════════════╣
                  누락된 변수: %s

                  해결:
                  1) 프로젝트 루트에 .env 파일 있는지 확인
                  2) IntelliJ Run Configuration → EnvFile 플러그인 활성화
                  3) docs/development.md 참고
                ╚══════════════════════════════════════════════════╝
                """.formatted(String.join(", ", missing));
            log.error(msg);
            throw new IllegalStateException("필수 환경변수 누락: " + missing);
        }

        String secret = env.getProperty("JWT_SECRET", "");
        if (secret.length() < JWT_SECRET_MIN_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET은 최소 %d자여야 합니다 (현재 %d자)"
                            .formatted(JWT_SECRET_MIN_LENGTH, secret.length())
            );
        }

        log.info("✅ 환경변수 검증 통과");
    }
}