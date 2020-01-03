package com.genius.primavera.application.validator;

import com.genius.primavera.domain.ExcelImportRequest;

public interface Validator {

	boolean validate(ExcelImportRequest excelImportRequest);
}