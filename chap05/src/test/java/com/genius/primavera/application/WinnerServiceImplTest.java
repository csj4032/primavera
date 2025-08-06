package com.genius.primavera.application;

import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testcontainers.EnableTestContainers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableTestContainers
@ActiveProfiles("test")
@DisplayName("WinnerServiceImpl 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerServiceImplTest {

    @Autowired
    private WinnerService winnerService;

    @Test
    @Order(1)
    @DisplayName("save 메소드 테스트")
    public void save() {
        Winner winner = Winner.builder().name("John Doe").year(2023).sport("Basketball").prize("Gold Medal").amount(new BigDecimal("10000.00")).build();
        int result = winnerService.save(winner);
        assertEquals(1, result, "Winner should be saved successfully");
    }

    @Test
    @Order(2)
    @DisplayName("saveAndNew 메소드 테스트")
    public void saveAndNew() {
        Winner winner1 = Winner.builder().name("Alice Smith").year(2023).sport("Tennis").prize("Silver Medal").amount(new BigDecimal("5000.00")).build();
        Winner winner2 = Winner.builder().name("Bob Johnson").year(2023).sport("Football").prize("Bronze Medal").amount(new BigDecimal("3000.00")).build();
        Winner winner3 = Winner.builder().name("Charlie Brown").year(2023).sport("Swimming").prize("Gold Medal").amount(new BigDecimal("7000.00")).build();
        int result = winnerService.saveAndNew(winner1, winner2, winner3, winnerService);
        assertEquals(0, result, "Winners should be saved with new transaction");
    }

    @Test
    @Order(3)
    @DisplayName("saveAndNested 메소드 테스트 - REQUIRES_NEW로 변경")
    public void saveAndNested() {
        Winner winner1 = Winner.builder().name("David Wilson").year(2023).sport("Athletics").prize("Gold Medal").amount(new BigDecimal("12000.00")).build();
        Winner winner2 = Winner.builder().name("Eva Green").year(2023).sport("Cycling").prize("Silver Medal").amount(new BigDecimal("8000.00")).build();
        Winner winner3 = Winner.builder().name("Frank White").year(2023).sport("Boxing").prize("Bronze Medal").amount(new BigDecimal("4000.00")).build();
        int result = winnerService.saveAndNested(winner1, winner2, winner3, winnerService);
        assertEquals(0, result, "Winners should be saved with new transaction (previously nested)");
    }

    @Test
    @Order(4)
    @DisplayName("saveAndNotSupported 메소드 테스트")
    public void saveAndNotSupported() {
        Winner winner1 = Winner.builder().name("Grace Lee").year(2023).sport("Volleyball").prize("Gold Medal").amount(new BigDecimal("11000.00")).build();
        Winner winner2 = Winner.builder().name("Hank Kim").year(2023).sport("Baseball").prize("Silver Medal").amount(new BigDecimal("6000.00")).build();
        Winner winner3 = Winner.builder().name("Ivy Park").year(2023).sport("Gymnastics").prize("Bronze Medal").amount(new BigDecimal("2000.00")).build();
        int result = winnerService.saveAndNotSupported(winner1, winner2, winner3, winnerService);
        assertEquals(0, result, "Winners should be saved with not supported transaction");
    }

    @Test
    @Order(5)
    @DisplayName("saveNotSupported 메소드 테스트 - NOT_SUPPORTED 트랜잭션")
    public void saveNotSupported() {
        Winner winner = Winner.builder()
                .name("Test Not Supported Winner")
                .year(2023)
                .sport("Test Sport")
                .prize("Test Prize")
                .amount(new BigDecimal("1000.00"))
                .build();
        int result = winnerService.saveNotSupported(winner);
        assertEquals(1, result, "Winner should be saved with NOT_SUPPORTED transaction");
    }

    @Test
    @Order(6)
    @DisplayName("saveNested 메소드 테스트 - REQUIRES_NEW 트랜잭션")
    public void saveNested() {
        Winner winner = Winner.builder()
                .name("Test Nested Winner")
                .year(2023)
                .sport("Test Sport")
                .prize("Test Prize")
                .amount(new BigDecimal("1000.00"))
                .build();
        int result = winnerService.saveNested(winner);
        assertEquals(0, result, "Winner should be saved with REQUIRES_NEW transaction");
    }

    @Test
    @Order(7)
    @DisplayName("saveRequiresNew 메소드 테스트 - REQUIRES_NEW 트랜잭션")
    public void saveRequiresNew() {
        Winner winner = Winner.builder()
                .name("Test Requires New Winner")
                .year(2023)
                .sport("Test Sport")
                .prize("Test Prize")
                .amount(new BigDecimal("1000.00"))
                .build();
        int result = winnerService.saveRequiresNew(winner);
        assertEquals(1, result, "Winner should be saved with REQUIRES_NEW transaction");
    }

    @Test
    @Order(8)
    @DisplayName("saveAll 메소드 테스트 - REQUIRES_NEW 트랜잭션")
    public void saveAll() {
        var winners = java.util.List.of(
                Winner.builder().name("Winner 1").year(2023).sport("Sport 1").prize("Prize 1").amount(new BigDecimal("1000.00")).build(),
                Winner.builder().name("Winner 2").year(2023).sport("Sport 2").prize("Prize 2").amount(new BigDecimal("2000.00")).build()
        );
        int result = winnerService.saveAll(winners);
        assertEquals(2, result, "All winners should be saved successfully");
    }

    @Test
    @Order(9)
    @DisplayName("saveAllNested 메소드 테스트 - REQUIRES_NEW로 변경")
    void saveAllNested() {
        var winners = java.util.List.of(
                Winner.builder().name("Winner 1").year(2023).sport("Sport 1").prize("Prize 1").amount(new BigDecimal("1000.00")).build(),
                Winner.builder().name("Winner 2").year(2023).sport("Sport 2").prize("Prize 2").amount(new BigDecimal("2000.00")).build()
        );
        int result = winnerService.saveAllNested(winners);
        assertEquals(2, result, "All winners should be saved with REQUIRES_NEW transaction");
    }

    @Test
    @Order(10)
    @DisplayName("innerSave 메소드 테스트 - REQUIRES_NEW 트랜잭션")
    public void innerSave() {
        var winners = java.util.List.of(
                Winner.builder().name("Inner Winner 1").year(2023).sport("Sport 1").prize("Prize 1").amount(new BigDecimal("1000.00")).build(),
                Winner.builder().name("Inner Winner 2").year(2023).sport("Sport 2").prize("Prize 2").amount(new BigDecimal("2000.00")).build()
        );
        int result = winnerService.innerSave(winners);
        assertEquals(2, result, "Inner winners should be saved successfully");
    }

    @Test
    @Order(11)
    @DisplayName("innerSaveNew 메소드 테스트 - REQUIRES_NEW 트랜잭션")
    public void innerSaveNew() {
        var winners = java.util.List.of(
                Winner.builder().name("Inner New Winner 1").year(2023).sport("Sport 1").prize("Prize 1").amount(new BigDecimal("1000.00")).build(),
                Winner.builder().name("Inner New Winner 2").year(2023).sport("Sport 2").prize("Prize 2").amount(new BigDecimal("2000.00")).build()
        );
        int result = winnerService.innerSaveNew(winners);
        assertEquals(2, result, "Inner new winners should be saved successfully");
    }

    @Test
    @Order(12)
    @DisplayName("innerNotSupported 메소드 테스트 - NOT_SUPPORTED 트랜잭션")
    public void innerNotSupported() {
        var winners = java.util.List.of(
                Winner.builder().name("Inner Not Supported Winner 1").year(2023).sport("Sport 1").prize("Prize 1").amount(new BigDecimal("1000.00")).build(),
                Winner.builder().name("Inner Not Supported Winner 2").year(2023).sport("Sport 2").prize("Prize 2").amount(new BigDecimal("2000.00")).build()
        );
        int result = winnerService.innerNotSupported(winners);
        assertEquals(2, result, "Inner not supported winners should be saved successfully");
    }

    @Test
    @Order(13)
    @DisplayName("findAllUncommitted 메소드 테스트")
    public void findAllUncommitted() {
        var winners = winnerService.findAllUncommitted();
        assertNotNull(winners, "Winners should be found with uncommitted isolation level");
        assertFalse(winners.isEmpty(), "Winners list should not be empty");
        for (Winner winner : winners) {
            assertNotNull(winner.getId(), "Winner ID should not be null");
            assertNotNull(winner.getSport(), "Winner sport should not be null");
            assertNotNull(winner.getPrize(), "Winner prize should not be null");
            assertNotNull(winner.getAmount(), "Winner amount should not be null");
        }
    }

    @Test
    @Order(14)
    @DisplayName("findAllCommitted 메소드 테스트")
    public void findAllCommitted() {
        var winners = winnerService.findAllCommitted();
        assertNotNull(winners, "Winners should be found with committed isolation level");
        assertFalse(winners.isEmpty(), "Winners list should not be empty");
        for (Winner winner : winners) {
            assertNotNull(winner.getId(), "Winner ID should not be null");
            assertNotNull(winner.getSport(), "Winner sport should not be null");
            assertNotNull(winner.getPrize(), "Winner prize should not be null");
            assertNotNull(winner.getAmount(), "Winner amount should not be null");
        }
    }

    @Test
    @Order(15)
    @DisplayName("findAllByIdReadCommitted 메소드 테스트")
    public void findAllByIdReadCommitted() {
        Long id = 1L; // 예시 ID, 실제 테스트에서는 적절한 ID로 변경 필요
        Winner winner = winnerService.findAllByIdReadCommitted(id);
        assertNotNull(winner, "Winner should be found by ID with Read Committed isolation level");
        assertEquals(id, winner.getId(), "Winner ID should match the requested ID");
        assertEquals("Football", winner.getSport(), "Winner sport should match the expected value");
        assertEquals("Ballon", winner.getPrize(), "Winner prize should match the expected value");
        assertEquals(new BigDecimal("10.00"), winner.getAmount(), "Winner amount should match the expected value");
    }

    @Test
    @Order(16)
    @DisplayName("findAllByIdRepeatableRead 메소드 테스트")
    public void findAllByIdRepeatableRead() {
        Long id = 1L; // 예시 ID, 실제 테스트에서는 적절한 ID로 변경 필요
        Winner winner = winnerService.findAllByIdRepeatableRead(id);
        assertNotNull(winner, "Winner should be found by ID with Repeatable Read isolation level");
        assertEquals(id, winner.getId(), "Winner ID should match the requested ID");
        assertEquals("Football", winner.getSport(), "Winner sport should match the expected value");
        assertEquals("Ballon", winner.getPrize(), "Winner prize should match the expected value");
        assertEquals(new BigDecimal("10.00"), winner.getAmount(), "Winner amount should match the expected value");
    }
}