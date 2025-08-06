package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountCSVParser;
import com.genius.primavera.application.account.AccountInfo;
import com.genius.primavera.application.account.AccountProcessor;
import com.genius.primavera.application.account.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.List;

@Slf4j
@DisplayName("계좌 파싱 클래스 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountCSVParserTest {

	private static String resource = "src/main/resources/";

	@Test
    @Order(1)
	@DisplayName("계좌 파싱 클래스 구현")
	public void accountCSVParsing() throws IOException {
		String fileName = "accountInfo.csv";
		List<String> lines = Files.readAllLines(Path.of(resource + fileName));
		AccountCSVParser accountCSVParser = new AccountCSVParser();
		List<AccountInfo> accountInfos = accountCSVParser.parseLinesFormCVS(lines);
		Assertions.assertNotNull(accountInfos);
		AccountProcessor accountPrecess = new AccountProcessor(accountInfos);
		Assertions.assertEquals(197300l, accountPrecess.calculationTotalAmount());
		Assertions.assertEquals(97300l, accountPrecess.calculationTotalForMonth(Month.JANUARY));
		Assertions.assertEquals(200000l, accountPrecess.calculationTotalForCategory(Category.Type0));
	}

    @Test
    @Order(2)
    @DisplayName("계좌 파싱 클래스 구현 실패")
    public void accountCSVParsingFail() {
        Assertions.assertThrows(IOException.class, () -> {
            String fileName = "nonExistentFile.csv";
            List<String> lines = Files.readAllLines(Path.of(resource + fileName));
            AccountCSVParser accountCSVParser = new AccountCSVParser();
            accountCSVParser.parseLinesFormCVS(lines);
        });
    }

    @Test
    @Order(3)
    @DisplayName("계좌 파싱 클래스 CSV 파싱")
    public void accountCSVParserTest() throws IOException {
        AccountCSVParser accountCSVParser = new AccountCSVParser();
        var lines = Files.readAllLines(Path.of(resource + "accountInfo.csv"));
        var accountInfos = accountCSVParser.parseLinesFormCVS(lines);
        Assertions.assertFalse(accountInfos.isEmpty(), "CSV 파싱 결과가 비어있지 않아야 합니다.");
    }
}
