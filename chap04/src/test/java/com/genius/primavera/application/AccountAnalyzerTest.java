package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountAnalyzer;
import com.genius.primavera.application.account.AccountCSVParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AccountAnalyzerTest {

	@Test
	@DisplayName("계좌 분석 클래스 구현")
	public void accountAnalyzerTest() throws IOException {
		AccountAnalyzer accountAnalyzer = new AccountAnalyzer("accountInfo.csv", new AccountCSVParser());
		Assertions.assertNotNull(accountAnalyzer);
	}
}
