package com.genius.primavera.application.account;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("계좌 정보 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountInfoTest {

    @Test
    @Order(1)
    @DisplayName("계좌 정보 생성 및 검증")
    public void accountInfoCreationTest() {
        var date = java.time.LocalDate.of(2023, 1, 1);
        long amount = 100000L;
        Category category = Category.Type0;
        AccountInfo accountInfo = new AccountInfo(date, amount, category);
        assertNotNull(accountInfo);
        assertEquals(date, accountInfo.date());
        assertEquals(amount, accountInfo.amount());
        assertEquals(category, accountInfo.category());
    }
}