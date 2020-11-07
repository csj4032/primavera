package com.genius.primavera.domain.model;

import com.genius.primavera.application.validator.Nickname;
import lombok.*;
import org.graalvm.polyglot.HostAccess;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
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
	private LocalDateTime regDate;
	private LocalDateTime modDate;

	private Boolean isComplex;

	@HostAccess.Export
	public Boolean isComplex(LocalDateTime regDate, LocalDateTime modDate) {
		if (!Objects.isNull(regDate) && !Objects.isNull(modDate)) return regDate.isBefore(modDate);
		return true;
	}
}