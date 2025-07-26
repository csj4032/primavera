package com.genius.primavera.domain;

import lombok.*;

import java.time.LocalDateTime;
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
    private List<String> roles; // javax.management.relation.Role을 String으로 변경
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(long id, String email, String password, String nickname, List<String> roles, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}