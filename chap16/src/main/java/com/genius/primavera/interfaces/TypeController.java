package com.genius.primavera.interfaces;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TypeController {

	@PostMapping(value = "/type", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> type(@RequestParam List<Long> strings) {
		System.out.println(strings);
		return new ResponseEntity<>("{\"name\":\"genius.choi\"}", HttpStatus.OK);
	}
}