package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountAnalyzer;
import com.genius.primavera.application.account.AccountCSVParser;
import org.junit.jupiter.api.*;

import java.io.IOException;

@DisplayName("계좌 분석 클래스 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountAnalyzerTest {

    @Test
    @Order(1)
    @DisplayName("계좌 분석 클래스 구현")
    public void accountAnalyzerTest() throws IOException {
        AccountAnalyzer accountAnalyzer = new AccountAnalyzer("accountInfo.csv", new AccountCSVParser());
        Assertions.assertNotNull(accountAnalyzer);
    }

    @Test
    @Order(2)
    @DisplayName("계좌 분석 클래스 구현 실패")
    public void accountAnalyzerTestFail() {
        Assertions.assertThrows(IOException.class, () -> {
            new AccountAnalyzer("nonExistentFile.csv", new AccountCSVParser());
        });
    }

    @Test
    @Order(3)
    @DisplayName("계좌 분석 클래스 CSV 파싱")
    public void accountCSVParserTest() throws IOException {
        AccountCSVParser accountCSVParser = new AccountCSVParser();
        var lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get("src/main/resources/accountInfo.csv"));
        var accountInfos = accountCSVParser.parseLinesFormCVS(lines);
        Assertions.assertFalse(accountInfos.isEmpty(), "CSV 파싱 결과가 비어있지 않아야 합니다.");
    }

    @Test
    @Order(4)
    @DisplayName("계좌 분석 클래스 CSV 파싱 실패")
    public void accountCSVParserTestFail() {
        Assertions.assertThrows(IOException.class, () -> {
            AccountCSVParser accountCSVParser = new AccountCSVParser();
            var lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get("src/main/resources/nonExistentFile.csv"));
            accountCSVParser.parseLinesFormCVS(lines);
        });
    }
}
