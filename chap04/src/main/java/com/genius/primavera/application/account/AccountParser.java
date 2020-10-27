package com.genius.primavera.application.account;

import java.util.List;

public interface AccountParser {

	AccountInfo parseFormCsv(String line);

	List<AccountInfo> parseLinesFormCVS(List<String> lines);
}
