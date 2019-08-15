package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.User;

import com.genius.primavera.domain.model.user.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<UserDto> findByNickname(String nickname, Class<? extends UserDto> type);
}
