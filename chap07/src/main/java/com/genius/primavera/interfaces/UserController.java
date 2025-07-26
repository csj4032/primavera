package com.genius.primavera.interfaces;

import com.genius.primavera.application.UserService;
import com.genius.primavera.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class UserController {

	private final UserService userService;

	@GetMapping
	public List<User> getUserById() {
		return userService.getUsers();
	}

	@GetMapping(value = "/{id}")
	public User getUserById(@PathVariable(value = "id") long id) {
		return userService.findById(id);
	}

	@PostMapping(value = "/save")
	public ResponseEntity<User> save(@RequestBody @Validated(User.SaveGroup.class) User user, BindingResult bindingResult) {
		log.info("{}", bindingResult);
		return getUserResponseEntity(user, bindingResult, HttpStatus.CREATED, userService::save);
	}

	@PostMapping(value = "/update")
	public ResponseEntity<User> update(@RequestBody @Validated(User.UpdateGroup.class) User user, BindingResult bindingResult) {
		log.info("{}", bindingResult);
		return getUserResponseEntity(user, bindingResult, HttpStatus.OK, userService::update);
	}

	@NotNull
	private ResponseEntity<User> getUserResponseEntity(User user, BindingResult bindingResult, HttpStatus httpStatus, Function<User, User> supplier) {
		return bindingResult.hasErrors() ? new ResponseEntity<>(user, HttpStatus.BAD_REQUEST) : new ResponseEntity<>(supplier.apply(user), httpStatus);
	}
}