package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.FileType;

public class UnknownFile extends AbstractResponseFactory {

	public UnknownFile(ExcelImportRequest excelImportRequest) {
		super(excelImportRequest);
	}

	@Override
	public ExcelImportResponse getExcelImportResponse() {
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), FileType.UNKNOWN, "Unknown file type");
	}
}