package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
}
