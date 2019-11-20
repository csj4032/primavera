package com.genius.primavera;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;

	@PostConstruct
	public void init() {
		userRepository.deleteAll();
		userRepository.save(new User(0L, "Winter", LocalDateTime.now()));
		userRepository.save(new User(1L, "Genius", LocalDateTime.now()));
		userRepository.save(new User(2L, "Spring", LocalDateTime.now()));
	}

	@GetMapping("/users")
	public ResponseEntity<Iterable<User>> getUsers() {
		return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
	}

	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUser(@PathVariable(value = "id") Long id) {
		return new ResponseEntity<>(userRepository.findById(id).get(), HttpStatus.OK);
	}
}