package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.UserMapper;
import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        user.setPassword(user.getPassword());
        user.setStatus(UserStatus.ON);
        user.setRegDate(LocalDateTime.now());
        userMapper.save(user);
        return user;
    }

    @Override
    public User update(User user) {
        if (null == userMapper.findById(user.getId())) {
            throw new NotFoundUserException(user);
        }
        userMapper.update(user);
        return user;
    }

    @Override
    public User findById(long id) {
        return userMapper.findById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public boolean signIn(String email, String password) {
        return userMapper.findByEmail(email).isAuthenticate(password);
    }

    @Override
    public List<User> getUsers() {
        return userMapper.findAll();
    }
}