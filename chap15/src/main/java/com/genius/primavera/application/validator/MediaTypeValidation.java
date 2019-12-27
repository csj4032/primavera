package com.genius.primavera.application.validator;

import com.genius.primavera.domain.ExcelImportRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;

import java.io.IOException;

@Slf4j
public class MediaTypeValidation implements Validator {

	private static final CharSequence APPLICATION_X_TIKA_OOXML = "application/x-tika-ooxml";

	@Override
	public boolean validate(ExcelImportRequest excelImportRequest) {
		log.info("Media Type Validator");
		try {
			return new Tika().detect(excelImportRequest.getInputStream()).contains(APPLICATION_X_TIKA_OOXML);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return false;
	}
}
