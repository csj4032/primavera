package com.genius.primavera.infrastructure.security.social;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2RestOperations;

public interface SocialAuthentication<T> {

	Authentication getAuthentication(Authentication authResult, OAuth2RestOperations restTemplate, Class<T> tClass);
}
