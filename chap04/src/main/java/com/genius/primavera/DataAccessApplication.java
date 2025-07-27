package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Data Access Application
 * 실행 하기 전에 Infrastructure docker-compose.yml 로 MariaDB을 실행해야 합니다.
 * 예시: docker compose -f docker-compose.yml up
 */
@Slf4j
@SpringBootApplication
public class DataAccessApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataAccessApplication.class, args);
    }
}