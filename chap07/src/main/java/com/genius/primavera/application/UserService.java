package com.genius.primavera.application;

import com.genius.primavera.domain.model.User;

import java.util.List;

public interface UserService {

    User save(User user);

    User update(User user);

    User findById(long id);

    User findByEmail(String email);

    boolean signIn(String email, String password);

    List<User> getUsers();

}
