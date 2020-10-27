package com.genius.primavera.application.account;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "date, amount, description")
public class AccountInfo {

	private final LocalDate date;
	private final long amount;
	private final Category category;

	public AccountInfo(LocalDate date, long amount, Category category) {
		this.date = date;
		this.amount = amount;
		this.category = category;
	}
}
