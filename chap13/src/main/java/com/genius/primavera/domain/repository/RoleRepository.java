package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.user.Role;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
