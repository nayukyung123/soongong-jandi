package com.soongongjandi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling   // daily_summary 일별 집계 스케줄러용
@SpringBootApplication
public class SoongongJandiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SoongongJandiApplication.class, args);
    }
}
