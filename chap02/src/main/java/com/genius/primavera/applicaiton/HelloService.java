package com.genius.primavera.applicaiton;

import com.genius.primavera.domain.User;

import java.util.List;

public interface HelloService {

	List<User> getUsers();

	User getUserById(Long id);

}