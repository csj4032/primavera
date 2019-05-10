package com.genius.primavera.application;

import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserConnection;

import java.util.Optional;

public interface UserService {

	User save(User user);

	User update(User user);

	User findByEmail(String email);

	User signUp(UserConnection userConnection);
}
