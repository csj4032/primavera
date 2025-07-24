package com.genius.primavera.application.validator;

import com.genius.primavera.domain.ExcelImportRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VersionValidation implements Validator {

	@Override
	public boolean validate(ExcelImportRequest excelImportRequest) {
		log.info("version validator");
		return false;
	}
}