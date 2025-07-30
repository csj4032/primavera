package com.genius.primavera.domain.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserConnection {
	private long id;
	private String email;
	private ProviderType provider;
	private String providerId;
	private String displayName;
	private String profileUrl;
	private String imageUrl;
	private String accessToken;
	private long expireTime;
}