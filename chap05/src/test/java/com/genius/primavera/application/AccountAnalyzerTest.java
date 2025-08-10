package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountAnalyzer;
import com.genius.primavera.application.account.AccountCSVParser;
import org.junit.jupiter.api.*;

import java.io.IOException;

@DisplayName("translated_text_2 translated_text_2 translated_text_3 test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountAnalyzerTest {

    @Test
    @Order(1)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 translated_text_2")
    public void accountAnalyzerTest() throws IOException {
        AccountAnalyzer accountAnalyzer = new AccountAnalyzer("accountInfo.csv", new AccountCSVParser());
        Assertions.assertNotNull(accountAnalyzer);
    }

    @Test
    @Order(2)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 translated_text_2 failure")
    public void accountAnalyzerTestFail() {
        Assertions.assertThrows(IOException.class, () -> {
            new AccountAnalyzer("nonExistentFile.csv", new AccountCSVParser());
        });
    }

    @Test
    @Order(3)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 CSV translated_text_2")
    public void accountCSVParserTest() throws IOException {
        AccountCSVParser accountCSVParser = new AccountCSVParser();
        var lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get("src/main/resources/accountInfo.csv"));
        var accountInfos = accountCSVParser.parseLinesFormCVS(lines);
        Assertions.assertFalse(accountInfos.isEmpty(), "CSV translated_text_2 translated_text_7 translated_text_4 translated_text_3 translated_text_3.");
    }

    @Test
    @Order(4)
    @DisplayName("translated_text_2 translated_text_2 translated_text_3 CSV translated_text_2 failure")
    public void accountCSVParserTestFail() {
        Assertions.assertThrows(IOException.class, () -> {
            AccountCSVParser accountCSVParser = new AccountCSVParser();
            var lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get("src/main/resources/nonExistentFile.csv"));
            accountCSVParser.parseLinesFormCVS(lines);
        });
    }
}
