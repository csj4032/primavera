package com.genius.primavera.application.factory;

import com.genius.primavera.application.template.FinancialTemplate;
import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.Financial;
import com.genius.primavera.domain.MediaType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ExcelTypeFile extends AbstractResponseFactory implements ResponseFactory {

	public ExcelTypeFile(ExcelImportRequest excelImportRequest) {
		super(excelImportRequest);
	}

	public ExcelImportResponse getExcelImportResponse() {
		List<Financial> financialList = new FinancialTemplate<Financial>(excelImportRequest.getInputStream()).read(Financial::of);
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.EXCEL_TYPE, "row count : " + financialList.size());
	}
}