package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportResponse;

@FunctionalInterface
public interface ResponseFactory {

	ExcelImportResponse getExcelImportResponse();
}
