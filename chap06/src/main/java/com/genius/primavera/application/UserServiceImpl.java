package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;

	@Override
	public User save(User user) {
		user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()));
		user.setStatus(UserStatus.ON);
		user.setRegDate(LocalDateTime.now());
		userMapper.save(user);
		return user;
	}

	@Override
	public User update(User user) {
		return user;
	}

	@Override
	public User signIn(String userId, String password) {
		return null;
	}

	@Override
	public List<User> getUsers() {
		return userMapper.findAll();
	}
}