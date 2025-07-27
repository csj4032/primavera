package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.MediaType;

public class SizeZeroFile extends AbstractResponseFactory {

	public SizeZeroFile(ExcelImportRequest excelImportRequest) {
		super(excelImportRequest);
	}

	public ExcelImportResponse getExcelImportResponse() {
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.UNKNOWN, "File Size 0");
	}
}
