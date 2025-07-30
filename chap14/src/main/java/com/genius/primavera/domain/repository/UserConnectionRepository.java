package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.UserConnection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
}
