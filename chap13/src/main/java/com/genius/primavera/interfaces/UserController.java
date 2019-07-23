package com.genius.primavera.interfaces;

import com.genius.primavera.application.user.UserService;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.infrastructure.aspect.PrimaveraLogging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	private UserService userService;

	@PrimaveraLogging(type = "UserController")
	@GetMapping(value = "/{id}")
	public User findById(@PathVariable(value = "id") long id) {
		return userService.findById(id);
	}

	@PostMapping(value = "/save")
	public ResponseEntity<User> save(@RequestBody User user, BindingResult bindingResult) {
		log.info("{}", bindingResult);
		if (!bindingResult.hasErrors()) {
			userService.save(user);
			return new ResponseEntity<>(user, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
	}

	@PostMapping(value = "/update")
	public ResponseEntity<User> update(@RequestBody User user, BindingResult bindingResult) {
		log.info("{}", bindingResult);
		if (!bindingResult.hasErrors()) {
			userService.update(user);
			return new ResponseEntity<>(user, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
	}
}