package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
public class AjaxController {

	@GetMapping(value = "/ajax")
	public String ajax() {
		return "ajax";
	}

	@GetMapping(value = "/ajax/html")
	public String html() {
		return "html";
	}

	@GetMapping(value = "/ajax/form")
	public @ResponseBody User form() {
		return User.builder().id(1).email("genius@gmail.com").build();
	}

	@GetMapping(value = "/ajax/form/data")
	public @ResponseBody User getFormData(User user) {
		return user;
	}

	@PostMapping(value = "/ajax/form/data")
	public @ResponseBody User postFormData(User user) {
		return user;
	}

	@GetMapping(value = "/ajax/json/data")
	public @ResponseBody User getJsonData(User user) {
		return user;
	}

	@GetMapping(value = "/ajax/json/data/body")
	public @ResponseBody User getJsonDataBody(@RequestBody User user) {
		return user;
	}

	@PostMapping(value = "/ajax/json/data")
	public @ResponseBody User postJsonData(@RequestBody User user) {
		return user;
	}

	@DeleteMapping(value = "/ajax/json/data")
	public @ResponseBody User deleteJsonData(@RequestBody User user) {
		return user;
	}

	@PostMapping(value = "/ajax/form/array")
	public @ResponseBody List<Long> getJsonArray(@RequestParam(name = "numbers[]") List<Long> numbers) {
		return numbers;
	}
}