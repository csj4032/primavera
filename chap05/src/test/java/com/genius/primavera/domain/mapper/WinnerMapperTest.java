package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testcontainer.EnablePrimaveraTestcontainers;
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
@EnablePrimaveraTestcontainers
@DisplayName(value = "벌크 인서트 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerMapperTest {

    @Autowired
    private WinnerMapper winnerMapper;

    @Test
    @Order(1)
    @DisplayName("반복문에서 단건으로 Winner 객체를 만들고 Insert 하는 테스트 (모던 스타일)")
    void testInsertSingleWinner() {
        // given
        var winners = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> Winner.builder()
                        .name("Winner " + i)
                        .year(2023)
                        .sport("Sport " + i)
                        .prize("Prize " + i)
                        .amount(BigDecimal.valueOf(1000 + i))
                        .build())
                .toList();

        // when & then
        winners.forEach(winner -> {
            int result = winnerMapper.save(winner);
            log.info("Inserted Winner: {}, Result: {}", winner, result);
            Assertions.assertEquals(1, result, "각 Winner는 1건씩 저장되어야 합니다.");
        });
    }

    @Test
    @Order(2)
    @DisplayName("벌크 인서트 테스트 (모던 스타일)")
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
        Assertions.assertEquals(10, result, "10개의 Winner가 벌크 인서트되어야 합니다.");
    }
}
