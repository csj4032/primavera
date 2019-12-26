package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.MediaType;

public class UnknownFile extends AbstractResponseFactory {

	public UnknownFile(ExcelImportRequest excelImportRequest) {
		super(excelImportRequest);
	}

	@Override
	public ExcelImportResponse getExcelImportResponse() {
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.UNKNOWN, "Unknown file type");
	}
}