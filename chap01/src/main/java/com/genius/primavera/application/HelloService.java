package com.genius.primavera.application;

import com.genius.primavera.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("helloService")
public class HelloService implements IHelloService {

	@Override
	public List<User> getUsers() {
		return Collections.EMPTY_LIST;
	}
}