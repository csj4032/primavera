package com.genius.primavera.application.account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Month;

public class AccountAnalyzer {

	private static String resource = "src/main/resources/";

	public AccountAnalyzer(String fineName, AccountParser accountParser) throws IOException {
		var path = Paths.get(resource + fineName);
		var lines = Files.readAllLines(path);
		var accountInfos = accountParser.parseLinesFormCVS(lines);
		var accountProcessor = new AccountPrecessor(accountInfos);
		collectSummary(accountProcessor);
	}

	private void collectSummary(AccountPrecessor accountProcessor) {
		System.out.println("Total : " + accountProcessor.calculationTotalAmount());
		System.out.println("Month : " + accountProcessor.calculationTotalForMonth(Month.JANUARY));
		System.out.println("Category : " + accountProcessor.calculationTotalForCategory(Category.Type0));
	}
}
