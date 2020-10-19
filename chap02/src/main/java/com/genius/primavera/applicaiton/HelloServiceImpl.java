package com.genius.primavera.applicaiton;

import com.genius.primavera.domain.model.User;
import com.genius.primavera.infrastructure.aspect.PrimaveraLogging;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HelloServiceImpl implements HelloService {

	@Override
	public List<User> getUsers() {
		return Collections.emptyList();
	}

	@Override
	@PrimaveraLogging(type = "Service")
	public User getUserById(Long id) {
		return new User(id);
	}
}