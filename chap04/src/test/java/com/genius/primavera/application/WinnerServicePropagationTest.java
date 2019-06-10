package com.genius.primavera.application;

import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.domain.model.WinnerType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName(value = "트랜잭션 전파 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerServicePropagationTest {

    @Autowired
    private WinnerService winnerService;

    @BeforeAll
    public static void setUp() {

    }

    @Test
    @Order(1)
    @DisplayName("PROPAGATION_REQUIRED")
    public void propagation_required() {
        Winner winner = Winner.builder().userId(1).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        winnerService.save(winner);
    }

    @Test
    @Order(2)
    @DisplayName("PROPAGATION_REQUIRES_NEW")
    public void propagation_requires_new() {
        Winner winner1 = Winner.builder().userId(1).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(2).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build();
        winnerService.saveAll(List.of(winner1, winner2));
    }

    @Test
    @Order(3)
    @DisplayName("PROPAGATION_REQUIRES_REQUIRES_NEW")
    public void propagation_required_requires_new() {
        Winner winner1 = Winner.builder().userId(4).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(5).winner(WinnerType.ETC).regDt(LocalDateTime.now()).build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.save(winner1);
            winnerService.saveAll(List.of(winner1, winner1));
            winnerService.saveAll(List.of(winner1, winner2));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(4)
    @DisplayName("PROPAGATION_REQUIRES_NOT_SUPPORTED")
    public void propagation_not_supported() {
        Winner winner1 = Winner.builder().userId(6).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(7).winner(WinnerType.ETC).regDt(LocalDateTime.now()).build();
        Winner winner3 = Winner.builder().userId(8).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.saveAllNotSupported(List.of(winner1, winner1));
            winnerService.saveAllNotSupported(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(5)
    @DisplayName("PROPAGATION_NESTED")
    public void propagation_nested() {
        Winner winner1 = Winner.builder().userId(9).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(10).winner(WinnerType.ETC).regDt(LocalDateTime.now()).build();
        Winner winner3 = Winner.builder().userId(11).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.saveAllNested(List.of(winner1, winner1));
            winnerService.saveAllNested(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(6)
    @DisplayName("PROPAGATION_REQUIRE_INNER")
    public void propagation_requires_inner() {
        Winner winner1 = Winner.builder().userId(12).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(13).winner(WinnerType.ETC).regDt(LocalDateTime.now()).build();
        Winner winner3 = Winner.builder().userId(14).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.innerSave(List.of(winner1, winner1));
            winnerService.innerSave(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(7)
    @DisplayName("PROPAGATION_REQUIRE_NEW_INNER")
    public void propagation_requires_new_inner() {
        Winner winner1 = Winner.builder().userId(15).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(16).winner(WinnerType.ETC).regDt(LocalDateTime.now()).build();
        Winner winner3 = Winner.builder().userId(17).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.innerSaveNew(List.of(winner1, winner1));
            winnerService.innerSaveNew(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(8)
    @DisplayName("PROPAGATION_NOT_SUPPORTED_INNER")
    public void propagation_not_supported_inner() {
        Winner winner1 = Winner.builder().userId(18).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build();
        Winner winner2 = Winner.builder().userId(19).winner(WinnerType.ETC).regDt(LocalDateTime.now()).build();
        Winner winner3 = Winner.builder().userId(20).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.innerNotSupported(List.of(winner1, winner1));
            winnerService.innerNotSupported(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }
}
