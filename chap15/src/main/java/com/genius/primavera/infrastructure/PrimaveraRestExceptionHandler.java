package com.genius.primavera.infrastructure;

import com.genius.primavera.interfaces.ExcelImportController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackageClasses = ExcelImportController.class)
public class PrimaveraRestExceptionHandler {

	@ExceptionHandler(PrimaveraBusinessException.class)
	protected ResponseEntity<Object> handleBusinessException(final PrimaveraBusinessException primaveraBusinessException) {
		log.error("PrimaveraBusinessException handleException Message {}", primaveraBusinessException.getMessage());
		return new ResponseEntity<>(primaveraBusinessException.getCause(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}