package com.genius.primavera.domain.model.user;

import lombok.*;

import javax.management.relation.Role;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(of = {"id", "email"})
public class User {
    private long id;
    private String email;
    private String password;
    private String nickname;
    private List<Role> roles;
    private LocalDateTime regDt;
    private LocalDateTime modDt;
}