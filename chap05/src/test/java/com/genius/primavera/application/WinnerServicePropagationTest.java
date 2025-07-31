package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.Winner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName(value = "트랜잭션 전파 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Complex transaction propagation test - requires database and transaction management")
public class WinnerServicePropagationTest {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    @Qualifier(value = "winnerService")
    private WinnerService winnerService;

    @BeforeAll
    public void setUp() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        WinnerMapper winnerMapper = sqlSession.getMapper(WinnerMapper.class);
        winnerMapper.truncate();
    }

    @Test
    @Order(1)
    @DisplayName("PROPAGATION_REQUIRED")
    public void propagation_required() {
        Winner winner = Winner.builder()
                .name("Tiger Woods")
                .year(2023)
                .sport("Golf")
                .prize("Masters Tournament")
                .amount(new BigDecimal("1500000.00"))
                .build();
        winnerService.save(winner);
    }

    @Test
    @Order(2)
    @DisplayName("PROPAGATION_REQUIRES_NEW")
    public void propagation_requires_new() {
        Winner winner1 = Winner.builder()
                .name("Serena Williams")
                .year(2023)
                .sport("Tennis")
                .prize("Wimbledon")
                .amount(new BigDecimal("750000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Rafael Nadal")
                .year(2023)
                .sport("Tennis")
                .prize("French Open")
                .amount(new BigDecimal("800000.00"))
                .build();
        winnerService.saveAll(List.of(winner1, winner2));
    }

    @Test
    @Order(3)
    @DisplayName("PROPAGATION_REQUIRES_REQUIRES_NEW")
    public void propagation_required_requires_new() {
        Winner winner1 = Winner.builder()
                .name("Lewis Hamilton")
                .year(2023)
                .sport("Formula 1")
                .prize("World Championship")
                .amount(new BigDecimal("2000000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Max Verstappen")
                .year(2023)
                .sport("Formula 1")
                .prize("Grand Prix Winner")
                .amount(new BigDecimal("1800000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Charles Leclerc")
                .year(2023)
                .sport("Formula 1")
                .prize("Pole Position Award")
                .amount(new BigDecimal("500000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> 
            winnerService.saveAndNew(winner1, winner2, winner3, winnerService)
        );
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(4)
    @DisplayName("PROPAGATION_REQUIRES_NESTED")
    public void propagation_required_nested() {
        Winner winner1 = Winner.builder()
                .name("Stephen Curry")
                .year(2023)
                .sport("Basketball")
                .prize("NBA Championship")
                .amount(new BigDecimal("1200000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("LeBron James")
                .year(2023)
                .sport("Basketball")
                .prize("NBA Finals MVP")
                .amount(new BigDecimal("1000000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Kevin Durant")
                .year(2023)
                .sport("Basketball")
                .prize("All-Star MVP")
                .amount(new BigDecimal("600000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> 
            winnerService.saveAndNested(winner1, winner2, winner3, winnerService)
        );
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(5)
    @DisplayName("PROPAGATION_REQUIRES_NESTED_REQUIRES")
    public void propagation_nested_required() {
        Winner winner1 = Winner.builder()
                .name("Cristiano Ronaldo")
                .year(2023)
                .sport("Football")
                .prize("UEFA Champions League")
                .amount(new BigDecimal("1100000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Kylian Mbappe")
                .year(2023)
                .sport("Football")
                .prize("World Cup Golden Boot")
                .amount(new BigDecimal("900000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Neymar Jr")
                .year(2023)
                .sport("Football")
                .prize("Copa America")
                .amount(new BigDecimal("850000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> 
            winnerService.saveAndNested(winner1, winner2, winner3, winnerService)
        );
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(6)
    @DisplayName("PROPAGATION_REQUIRES_NOT_SUPPORTED")
    public void propagation_not_supported() {
        Winner winner1 = Winner.builder()
                .name("Novak Djokovic")
                .year(2023)
                .sport("Tennis")
                .prize("Australian Open")
                .amount(new BigDecimal("950000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Carlos Alcaraz")
                .year(2023)
                .sport("Tennis")
                .prize("US Open")
                .amount(new BigDecimal("900000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Daniil Medvedev")
                .year(2023)
                .sport("Tennis")
                .prize("ATP Finals")
                .amount(new BigDecimal("700000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> 
            winnerService.saveAndNotSupported(winner1, winner2, winner3, winnerService)
        );
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(7)
    @DisplayName("PROPAGATION_REQUIRE_INNER")
    public void propagation_requires_inner() {
        Winner winner1 = Winner.builder()
                .name("Michael Phelps")
                .year(2023)
                .sport("Swimming")
                .prize("World Championship")
                .amount(new BigDecimal("650000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Katie Ledecky")
                .year(2023)
                .sport("Swimming")
                .prize("Olympic Gold")
                .amount(new BigDecimal("550000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Caeleb Dressel")
                .year(2023)
                .sport("Swimming")
                .prize("World Record")
                .amount(new BigDecimal("400000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.innerSave(List.of(winner1, winner1));
            winnerService.innerSave(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(8)
    @DisplayName("PROPAGATION_REQUIRE_NEW_INNER")
    public void propagation_requires_new_inner() {
        Winner winner1 = Winner.builder()
                .name("Usain Bolt")
                .year(2023)
                .sport("Athletics")
                .prize("World Record")
                .amount(new BigDecimal("800000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Elaine Thompson-Herah")
                .year(2023)
                .sport("Athletics")
                .prize("Olympic Gold")
                .amount(new BigDecimal("600000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Sydney McLaughlin")
                .year(2023)
                .sport("Athletics")
                .prize("World Championship")
                .amount(new BigDecimal("550000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.innerSaveNew(List.of(winner1, winner1));
            winnerService.innerSaveNew(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }

    @Test
    @Order(9)
    @DisplayName("PROPAGATION_NOT_SUPPORTED_INNER")
    public void propagation_not_supported_inner() {
        Winner winner1 = Winner.builder()
                .name("Simone Biles")
                .year(2023)
                .sport("Gymnastics")
                .prize("World Championship")
                .amount(new BigDecimal("750000.00"))
                .build();
        Winner winner2 = Winner.builder()
                .name("Sunisa Lee")
                .year(2023)
                .sport("Gymnastics")
                .prize("Olympic Gold")
                .amount(new BigDecimal("600000.00"))
                .build();
        Winner winner3 = Winner.builder()
                .name("Jordan Chiles")
                .year(2023)
                .sport("Gymnastics")
                .prize("Team Gold")
                .amount(new BigDecimal("450000.00"))
                .build();
        Exception exception = assertThrows(DataIntegrityViolationException.class, () -> {
            winnerService.innerNotSupported(List.of(winner1, winner1));
            winnerService.innerNotSupported(List.of(winner1, winner2, winner3));
        });
        assertEquals(DataIntegrityViolationException.class, exception.getClass());
    }
}
