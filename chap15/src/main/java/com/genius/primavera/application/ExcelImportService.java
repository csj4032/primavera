package com.genius.primavera.application;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;


import java.io.IOException;

public interface ExcelImportService {

	default long getFileSize(ExcelFileValid excelFileValid) throws IOException {
		if (excelFileValid.getInputStream() == null) return 0;
		return IOUtils.toByteArray(excelFileValid.getInputStream()).length;
	}

	default String getMediaType(ExcelFileValid excelFileValid) throws IOException {
		return new Tika().detect(excelFileValid.getInputStream());
	}

	ExcelImportResponse excelImport(ExcelImportRequest excelImportRequest) throws IOException;
}
