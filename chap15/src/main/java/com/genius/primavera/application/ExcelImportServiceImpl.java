package com.genius.primavera.application;

import com.genius.primavera.application.factory.ExcelTypeFile;
import com.genius.primavera.application.factory.ResponseFactory;
import com.genius.primavera.application.factory.SizeZeroFile;
import com.genius.primavera.application.factory.UnknownFile;
import com.genius.primavera.application.validator.Validator;
import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {

	private final Map<String, List<Validator>> validators;

	@Override
	public ExcelImportResponse excelImport(ExcelImportRequest excelImportRequest) {
		boolean valid = validators.get("sizeAndTypeValidation").stream().allMatch(v -> v.validate(excelImportRequest));
		log.info("valid : {}", valid);
		if (valid) {
			return new ExcelTypeFile(excelImportRequest).getExcelImportResponse();
		} else {
			return new UnknownFile(excelImportRequest).getExcelImportResponse();
		}
	}
}
