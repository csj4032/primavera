package com.genius.primavera.application.account;

import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

public class AccountPrecessor {

	private final List<AccountInfo> accountInfos;

	public AccountPrecessor(List<AccountInfo> accountInfos) {
		this.accountInfos = accountInfos;
	}

	public long calculationTotalAmount() {
		return accountInfos.stream().map(e -> e.getAmount()).collect(Collectors.summingLong(Long::longValue));
	}

	public long calculationTotalForCategory(Category category) {
		return accountInfos.stream().filter(e -> e.getCategory().equals(category)).map(e -> e.getAmount()).reduce(0l, Long::sum);
	}

	public long calculationTotalForMonth(Month month) {
		return accountInfos.stream().filter(e -> e.getDate().getMonth().equals(month)).map(e -> e.getAmount()).reduce(0l, (a, b) -> a + b);
	}
}