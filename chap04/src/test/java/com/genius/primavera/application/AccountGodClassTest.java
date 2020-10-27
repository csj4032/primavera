package com.genius.primavera.application;

import com.genius.primavera.application.account.AccountGodClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Slf4j
public class AccountGodClassTest {

	@Test
	@DisplayName("계좌 파싱 갓 클래스 구현")
	public void accountGodClassTest() throws IOException {
		String path = "/accountInfo.csv";
		AccountGodClass accountTransaction = new AccountGodClass(path);
		accountTransaction.calculation();
		log.info("total : {}", accountTransaction.getTotal());
		Assertions.assertEquals(97300l, accountTransaction.getTotal());
	}
}
