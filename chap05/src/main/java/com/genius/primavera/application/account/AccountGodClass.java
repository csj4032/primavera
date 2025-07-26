package com.genius.primavera.application.account;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AccountGodClass {

	private long total;
	private String fileName;
	private static String resource = "src/main/resources/";

	public AccountGodClass(String fileName) {
		this.fileName = fileName;
	}

	public void calculation() throws IOException {
		Path path = Paths.get(resource + fileName);
		List<String> lines = Files.readAllLines(path);
		for (String line : lines) {
			var columns = line.split(",");
			var amount = Long.parseLong(columns[1]);
			total += amount;
		}
	}

	public Long getTotal() {
		return total;
	}
}
