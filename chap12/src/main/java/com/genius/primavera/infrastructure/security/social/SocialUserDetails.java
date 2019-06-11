package com.genius.primavera.infrastructure.security.social;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

public interface SocialUserDetails {

	void setAccessToken(OAuth2AccessToken accessToken);
}
