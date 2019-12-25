package com.genius.primavera.application;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;

public interface ExcelImportService {

	ExcelImportResponse excelImport(ExcelImportRequest excelImportRequest);
}
