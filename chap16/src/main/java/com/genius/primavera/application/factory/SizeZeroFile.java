package com.genius.primavera.application.factory;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.FileType;

public class SizeZeroFile extends AbstractResponseFactory {

	public SizeZeroFile(ExcelImportRequest excelImportRequest) {
		super(excelImportRequest);
	}

	public ExcelImportResponse getExcelImportResponse() {
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), FileType.UNKNOWN, "File Size 0");
	}
}
