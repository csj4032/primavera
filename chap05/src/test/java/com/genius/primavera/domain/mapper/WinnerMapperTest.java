package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Winner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName(value = "translated_text_2 translated_text_3 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerMapperTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Autowired
    private WinnerMapper winnerMapper;

    @Test
    @Order(1)
    @DisplayName("translated_text_5 translated_text_4 Winner translated_text_3 translated_text_3 Insert translated_text_2 test (translated_text_2 translated_text_3)")
    void testInsertSingleWinner() {

        var winners = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> Winner.builder()
                        .name("Winner " + i)
                        .year(2023)
                        .sport("Sport " + i)
                        .prize("Prize " + i)
                        .amount(BigDecimal.valueOf(1000 + i))
                        .build())
                .toList();

        winners.forEach(winner -> {
            int result = winnerMapper.save(winner);
            log.info("Inserted Winner: {}, Result: {}", winner, result);
            Assertions.assertEquals(1, result, "translated_text_1 Winnertranslated_text_1 1translated_text_2 translated_text_5 translated_text_3.");
        });
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_3 test (translated_text_2 translated_text_3)")
    void testBulkInsertWinners() {
        List<Winner> winners = IntStream.range(0, 10)
                .mapToObj(i -> Winner.builder()
                        .name("Winner " + i)
                        .year(2023)
                        .sport("Sport " + i)
                        .prize("Prize " + i)
                        .amount(BigDecimal.valueOf(1000 + i))
                        .build())
                .toList();
        int result = winnerMapper.bulkInsert(winners);
        log.info("Bulk Insert Result: {}", result);
        Assertions.assertEquals(10, result, "10translated_text_2 Winnertranslated_text_1 translated_text_2 translated_text_3 translated_text_3.");
    }
}
