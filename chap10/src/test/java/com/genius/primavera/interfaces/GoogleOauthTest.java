package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GoogleOauthTest {

	@Test
	@Order(1)
	@DisplayName("OAuth2 translated_text_5 registration translated_text_2 test")
	public void oauthClientRegistrationTest() {
		ClientRegistration.Builder builder = ClientRegistration.withRegistrationId("google")
				.clientId("test-client-id")
				.clientSecret("test-client-secret")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
				.tokenUri("https://www.googleapis.com/oauth2/v4/token")
				.userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
				.userNameAttributeName("sub")
				.clientName("Google");

		ClientRegistration registration = builder.build();
		
		assertNotNull(registration);
		assertEquals("google", registration.getRegistrationId());
		assertEquals("test-client-id", registration.getClientId());
		assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE, registration.getAuthorizationGrantType());
	}

	@Test
	@Order(2)
	@DisplayName("OAuth2 URL translated_text_2 test")
	public void oauthUrlConfigurationTest() {
		String authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth";
		String tokenUri = "https://www.googleapis.com/oauth2/v4/token";
		String userInfoUri = "https://www.googleapis.com/oauth2/v3/userinfo";
		
		assertTrue(authorizationUri.startsWith("https://"));
		assertTrue(tokenUri.startsWith("https://"));
		assertTrue(userInfoUri.startsWith("https://"));
		
		assertTrue(authorizationUri.contains("accounts.google.com"));
		assertTrue(tokenUri.contains("googleapis.com"));
		assertTrue(userInfoUri.contains("googleapis.com"));
	}
}