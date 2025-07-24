package com.genius.primavera.application.validator;

import com.genius.primavera.domain.ExcelImportRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

@Slf4j
public class FileSizeValidator implements Validator {

	@Override
	public boolean validate(ExcelImportRequest excelImportRequest) {
		log.info("File Size Validator");
		try {
			long size = IOUtils.toByteArray(excelImportRequest.getInputStream()).length;
			log.info("File Size : {}", size);
			return size > 0 ? true : false;
		} catch (IOException e) {
			log.error(e.getMessage());
			return false;
		}
	}
}
