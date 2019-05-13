package com.genius.primavera.infrastructure.security;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserStatus;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class PrimaveraUserDetails implements UserDetails {

	private User user;

	public PrimaveraUserDetails(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getType().toString())).collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return !user.getStatus().equals(UserStatus.LEAVE);
	}

	@Override
	public boolean isAccountNonLocked() {
		return !user.getStatus().equals(UserStatus.BLOCK);
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return !user.getStatus().equals(UserStatus.DORMANT);
	}

	@Override
	public boolean isEnabled() {
		return user.getStatus().equals(UserStatus.ON);
	}

	public String getImageUrl() {
		return user.getConnection().getImageUrl();
	}

	public static PrimaveraUserDetails of(User user) {
		return new PrimaveraUserDetails(user);
	}
}
