package com.genius.primavera.interfaces;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TypeController {

	@GetMapping(value = "/type", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> type() {
		return new ResponseEntity<>("{\"name\":\"genius.choi\"}", HttpStatus.OK);
	}
}