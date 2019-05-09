package com.genius.primavera.application;

import com.genius.primavera.domain.model.User;

public interface UserService {

	User save(User user);

	User update(User user);

	User findByEmail(String email);
}
