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
@DisplayName("WinnerServiceImpl test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerServiceImplTest {

    @Autowired
    private WinnerService winnerService;

    @Test
    @Order(1)
    @DisplayName("save translated_text_3 test")
    public void save() {
        Winner winner = Winner.builder().name("John Doe").year(2023).sport("Basketball").prize("Gold Medal").amount(new BigDecimal("10000.00")).build();
        int result = winnerService.save(winner);
        assertEquals(1, result, "Winner should be saved successfully");
    }

    @Test
    @Order(2)
    @DisplayName("saveAndNew translated_text_3 test")
    public void saveAndNew() {
        Winner winner1 = Winner.builder().name("Alice Smith").year(2023).sport("Tennis").prize("Silver Medal").amount(new BigDecimal("5000.00")).build();
        Winner winner2 = Winner.builder().name("Bob Johnson").year(2023).sport("Football").prize("Bronze Medal").amount(new BigDecimal("3000.00")).build();
        Winner winner3 = Winner.builder().name("Charlie Brown").year(2023).sport("Swimming").prize("Gold Medal").amount(new BigDecimal("7000.00")).build();
        int result = winnerService.saveAndNew(winner1, winner2, winner3, winnerService);
        assertEquals(0, result, "Winners should be saved with new transaction");
    }

    @Test
    @Order(3)
    @DisplayName("saveAndNested translated_text_3 test - REQUIRES_NEWtranslated_text_1 translated_text_2")
    public void saveAndNested() {
        Winner winner1 = Winner.builder().name("David Wilson").year(2023).sport("Athletics").prize("Gold Medal").amount(new BigDecimal("12000.00")).build();
        Winner winner2 = Winner.builder().name("Eva Green").year(2023).sport("Cycling").prize("Silver Medal").amount(new BigDecimal("8000.00")).build();
        Winner winner3 = Winner.builder().name("Frank White").year(2023).sport("Boxing").prize("Bronze Medal").amount(new BigDecimal("4000.00")).build();
        int result = winnerService.saveAndNested(winner1, winner2, winner3, winnerService);
        assertEquals(0, result, "Winners should be saved with new transaction (previously nested)");
    }

    @Test
    @Order(4)
    @DisplayName("saveAndNotSupported translated_text_3 test")
    public void saveAndNotSupported() {
        Winner winner1 = Winner.builder().name("Grace Lee").year(2023).sport("Volleyball").prize("Gold Medal").amount(new BigDecimal("11000.00")).build();
        Winner winner2 = Winner.builder().name("Hank Kim").year(2023).sport("Baseball").prize("Silver Medal").amount(new BigDecimal("6000.00")).build();
        Winner winner3 = Winner.builder().name("Ivy Park").year(2023).sport("Gymnastics").prize("Bronze Medal").amount(new BigDecimal("2000.00")).build();
        int result = winnerService.saveAndNotSupported(winner1, winner2, winner3, winnerService);
        assertEquals(0, result, "Winners should be saved with not supported transaction");
    }

    @Test
    @Order(5)
    @DisplayName("saveNotSupported translated_text_3 test - NOT_SUPPORTED translated_text_4")
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
    @DisplayName("saveNested translated_text_3 test - REQUIRES_NEW translated_text_4")
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
    @DisplayName("saveRequiresNew translated_text_3 test - REQUIRES_NEW translated_text_4")
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
    @DisplayName("saveAll translated_text_3 test - REQUIRES_NEW translated_text_4")
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
    @DisplayName("saveAllNested translated_text_3 test - REQUIRES_NEWtranslated_text_1 translated_text_2")
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
    @DisplayName("innerSave translated_text_3 test - REQUIRES_NEW translated_text_4")
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
    @DisplayName("innerSaveNew translated_text_3 test - REQUIRES_NEW translated_text_4")
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
    @DisplayName("innerNotSupported translated_text_3 test - NOT_SUPPORTED translated_text_4")
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
    @DisplayName("findAllUncommitted translated_text_3 test")
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
    @DisplayName("findAllCommitted translated_text_3 test")
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
    @DisplayName("findAllByIdReadCommitted translated_text_3 test")
    public void findAllByIdReadCommitted() {
        Long id = 1L;
        Winner winner = winnerService.findAllByIdReadCommitted(id);
        assertNotNull(winner, "Winner should be found by ID with Read Committed isolation level");
        assertEquals(id, winner.getId(), "Winner ID should match the requested ID");
        assertEquals("Football", winner.getSport(), "Winner sport should match the expected value");
        assertEquals("Ballon", winner.getPrize(), "Winner prize should match the expected value");
        assertEquals(new BigDecimal("10.00"), winner.getAmount(), "Winner amount should match the expected value");
    }

    @Test
    @Order(16)
    @DisplayName("findAllByIdRepeatableRead translated_text_3 test")
    public void findAllByIdRepeatableRead() {
        Long id = 1L;
        Winner winner = winnerService.findAllByIdRepeatableRead(id);
        assertNotNull(winner, "Winner should be found by ID with Repeatable Read isolation level");
        assertEquals(id, winner.getId(), "Winner ID should match the requested ID");
        assertEquals("Football", winner.getSport(), "Winner sport should match the expected value");
        assertEquals("Ballon", winner.getPrize(), "Winner prize should match the expected value");
        assertEquals(new BigDecimal("10.00"), winner.getAmount(), "Winner amount should match the expected value");
    }
}