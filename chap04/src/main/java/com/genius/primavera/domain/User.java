package com.genius.primavera.domain;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "email"})
public class User {
    private long id;
    private String email;
    private String password;
    private String nickname;
    private UserStatus status;
    private List<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}