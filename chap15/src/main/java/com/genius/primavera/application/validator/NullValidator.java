package com.genius.primavera.application.validator;

import com.genius.primavera.domain.ExcelImportRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NullValidator implements Validator {

	@Override
	public boolean validate(ExcelImportRequest excelImportRequest) {
		log.info("Null Validation");
		return excelImportRequest != null;
	}
}
