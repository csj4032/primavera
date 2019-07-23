package com.genius.primavera.application.user;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserConnection;

public interface UserService {

	User save(User user);

	User update(User user);

	User findByEmail(String email);

	User signUp(UserConnection userConnection);

	boolean isExistUser(String email);

    User findById(long id);
}
