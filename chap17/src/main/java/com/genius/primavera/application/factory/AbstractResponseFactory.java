package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractResponseFactory implements ResponseFactory {

	protected final ExcelImportRequest excelImportRequest;

	public AbstractResponseFactory(ExcelImportRequest excelImportRequest) {
		this.excelImportRequest = excelImportRequest;
	}
}
