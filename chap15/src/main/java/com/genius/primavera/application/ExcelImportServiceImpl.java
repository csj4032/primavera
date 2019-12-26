package com.genius.primavera.application;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ExcelImportServiceImpl implements ExcelImportService {

	@Override
	public ExcelImportResponse excelImport(ExcelImportRequest excelImportRequest) throws IOException {
		if (isEmpty(excelImportRequest)) return getSizeZeroFile(excelImportRequest);
		if (isExcelFile(excelImportRequest))
			return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.EXCEL_TYPE, "");
		return getUnknownFile(excelImportRequest);
	}

	private ExcelImportResponse getSizeZeroFile(ExcelImportRequest excelImportRequest) {
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.UNKNOWN, "File Size 0");
	}

	private ExcelImportResponse getUnknownFile(ExcelImportRequest excelImportRequest) {
		return new ExcelImportResponse(excelImportRequest.getName(), excelImportRequest.getSize(), MediaType.UNKNOWN, "Unknown file type");
	}
}
