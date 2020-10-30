package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountCSVParser;
import com.genius.primavera.application.account.AccountInfo;
import com.genius.primavera.application.account.AccountProcessor;
import com.genius.primavera.application.account.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.List;

@Slf4j
public class AccountCSVParserTest {

	private static String resource = "src/main/resources/";

	@Test
	@DisplayName("계좌 파싱 클래스 구현")
	public void accountCSVParsing() throws IOException {
		String fileName = "accountInfo.csv";
		List<String> lines = Files.readAllLines(Path.of(resource + fileName));
		AccountCSVParser accountCSVParser = new AccountCSVParser();
		List<AccountInfo> accountInfos = accountCSVParser.parseLinesFormCVS(lines);
		Assertions.assertNotNull(accountInfos);
		AccountProcessor accountPrecess = new AccountProcessor(accountInfos);
		Assertions.assertEquals(97300l, accountPrecess.calculationTotalAmount());
		Assertions.assertEquals(97300l, accountPrecess.calculationTotalForMonth(Month.JANUARY));
		Assertions.assertEquals(100000l, accountPrecess.calculationTotalForCategory(Category.Type0));
	}
}
