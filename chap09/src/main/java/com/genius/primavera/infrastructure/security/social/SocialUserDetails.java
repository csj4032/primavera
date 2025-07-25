package com.genius.primavera.infrastructure.security.social;

public interface SocialUserDetails {

	void setAccessToken(String accessToken);
	void setExpiration(long expiration);
}
