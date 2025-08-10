package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountGodClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.IOException;

@Slf4j
@DisplayName("translated_text_2 translated_text_2 translated_text_3 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountGodClassTest {

    @Test
    @Order(1)
    @DisplayName("translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_2")
    public void accountGodClassTest() throws IOException {
        String path = "/accountInfo.csv";
        AccountGodClass accountTransaction = new AccountGodClass(path);
        accountTransaction.calculation();
        log.info("total : {}", accountTransaction.getTotal());
        Assertions.assertEquals(197300l, accountTransaction.getTotal());
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_1 translated_text_3 translated_text_2 failure")
    public void accountGodClassTestFail() {
        Assertions.assertThrows(IOException.class, () -> {
            String path = "/nonExistentFile.csv";
            AccountGodClass accountTransaction = new AccountGodClass(path);
            accountTransaction.calculation();
        });
    }
}
