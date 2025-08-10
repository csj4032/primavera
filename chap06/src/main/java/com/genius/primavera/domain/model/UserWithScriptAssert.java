package com.genius.primavera.domain.model;

import lombok.*;
import org.hibernate.validator.constraints.ScriptAssert;

import jakarta.validation.constraints.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@ScriptAssert(
    lang = "groovy",
    script = "_this.password != null && _this.passwordConfirm != null && _this.password.equals(_this.passwordConfirm)",
    alias = "_this",
    message = "Passwords do not match"
)
public class UserWithScriptAssert {

    @Min(value = 1)
    private long id;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,20}$")
    private String password;
    
    @NotBlank
    private String passwordConfirm;
    
    @NotBlank
    private String nickname;
}