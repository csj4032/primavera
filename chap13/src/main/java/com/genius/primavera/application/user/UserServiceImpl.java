package com.genius.primavera.application.user;

import com.genius.primavera.domain.model.user.Role;
import com.genius.primavera.domain.model.user.RoleType;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserConnection;
import com.genius.primavera.domain.model.user.UserStatus;
import com.genius.primavera.domain.repository.UserRepository;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User signUp(UserConnection userConnection) {
        var user = User.builder()
                .email(userConnection.getEmail())
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .nickname(userConnection.getDisplayName())
                .status(UserStatus.ON)
                .build();
        user.setConnection(userConnection);
        user.setRoles(Set.of(Role.builder().type(RoleType.USER).build()));
        return userRepository.save(user);
    }

    @Override
    public boolean isExistUser(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public User findById(long id) {
        return userRepository.findById(id).orElse(new User());
    }

    @Override
    @Transactional
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    }
}