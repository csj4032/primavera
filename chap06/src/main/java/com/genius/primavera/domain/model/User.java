package com.genius.primavera.domain.model;

import com.genius.primavera.application.validator.Nickname;
import com.genius.primavera.application.validator.PasswordMatch;
import lombok.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
@PasswordMatch
public class User {

    public interface SaveGroup extends Default {
    }

    public interface UpdateGroup extends Default {
    }

    @Min(value = 1, groups = UpdateGroup.class)
    private long id;
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class})
    @Email(groups = {SaveGroup.class, UpdateGroup.class})
    private String email;
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class})
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,20}$", 
             message = "{user.validation.password.pattern}",
             groups = {SaveGroup.class, UpdateGroup.class})
    private String password;
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class})
    private String passwordConfirm;
    @NotBlank(groups = {SaveGroup.class, UpdateGroup.class})
    @Nickname(groups = {SaveGroup.class, UpdateGroup.class})
    private String nickname;
    @NotNull(groups = UpdateGroup.class)
    private UserStatus status;
    @Valid
    @NotNull(groups = {SaveGroup.class, UpdateGroup.class})
    @Size(min = 1, groups = {SaveGroup.class, UpdateGroup.class})
    private List<Role> roles;
    private Instant createdAt;
    private Instant updatedAt;

    private Boolean isComplex;

    public Boolean isComplex(Instant createdAt, Instant updatedAt) {
        if (!Objects.isNull(createdAt) && !Objects.isNull(updatedAt)) return createdAt.isBefore(updatedAt);
        return true;
    }
}