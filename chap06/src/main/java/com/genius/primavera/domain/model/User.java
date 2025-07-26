package com.genius.primavera.domain.model;

import com.genius.primavera.application.validator.Nickname;
import lombok.*;
import org.graalvm.polyglot.HostAccess;
import org.hibernate.validator.constraints.ScriptAssert;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "email"})
@ScriptAssert(lang = "graal.js", script = "_this.isComplex(_this.regDate, _this.modDate)", message = "등록일자와 수정일자는 필수 입니다.")
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isComplex;

    @HostAccess.Export
    public Boolean isComplex(LocalDateTime createdAt, LocalDateTime modDate) {
        if (!Objects.isNull(createdAt) && !Objects.isNull(modDate)) return createdAt.isBefore(modDate);
        return true;
    }
}