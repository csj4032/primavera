package com.genius.primavera.application.account;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AccountCSVParser implements AccountParser {

	private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Override
	public AccountInfo parseFormCsv(String line) {
		var columns = line.split(",");
		var date = LocalDate.parse(columns[0], DATE_PATTERN);
		var amount = Long.parseLong(columns[1]);
		var category = Category.valueOf(columns[2]);
		return new AccountInfo(date, amount, category);
	}

	@Override
	public List<AccountInfo> parseLinesFormCVS(List<String> lines) {
		List<AccountInfo> accountInfos = new ArrayList<>();
		for (String line : lines) {
			accountInfos.add(parseFormCsv(line));
		}
		return accountInfos;
	}
}