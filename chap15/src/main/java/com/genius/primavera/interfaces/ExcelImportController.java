package com.genius.primavera.interfaces;

import com.genius.primavera.domain.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExcelImportController {

	@PostMapping("/save")
	public ResponseEntity<Message> save(ExcelImportRequest excelRequest) {
		return new ResponseEntity<>(new Message(), HttpStatus.CREATED);
	}
}
