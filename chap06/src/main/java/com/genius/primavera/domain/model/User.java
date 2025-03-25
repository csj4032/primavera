package com.genius.primavera.domain.model;

import com.genius.primavera.application.validator.Nickname;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.lang.NonNull;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
public class User {

    public interface SaveGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

    @Min(value = 1, groups = UpdateGroup.class)
    private long id;
    @Email
    private String email;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,20}$")
    private String password;
    @Nickname
    private String nickname;
    @NotNull(groups = UpdateGroup.class)
    private UserStatus status;
    @Valid
    @NotNull
    @Size(min = 1)
    private List<Role> roles;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

    public boolean isAuthenticate(@NonNull String password) {
        return new BCryptPasswordEncoder().matches(password, this.password);
    }
}