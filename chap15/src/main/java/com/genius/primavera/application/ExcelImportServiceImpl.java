package com.genius.primavera.application;

import com.genius.primavera.application.factory.ExcelTypeFile;
import com.genius.primavera.application.factory.ResponseFactory;
import com.genius.primavera.application.factory.SizeZeroFile;
import com.genius.primavera.application.factory.UnknownFile;
import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ExcelImportServiceImpl implements ExcelImportService {

	@Override
	public ExcelImportResponse excelImport(ExcelImportRequest excelImportRequest) {
		return this.getResponseFactory(excelImportRequest).getExcelImportResponse();
	}

	private ResponseFactory getResponseFactory(ExcelImportRequest excelImportRequest) {
		try {
			if (isEmpty(excelImportRequest)) return new SizeZeroFile(excelImportRequest);
			if (isExcelFile(excelImportRequest)) return new ExcelTypeFile(excelImportRequest);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return new UnknownFile(excelImportRequest);
	}
}
