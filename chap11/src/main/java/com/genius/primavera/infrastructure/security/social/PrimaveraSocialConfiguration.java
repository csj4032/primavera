package com.genius.primavera.infrastructure.security.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.user.UserConnection;
import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import com.genius.primavera.infrastructure.security.social.facebook.FacebookUserDetails;
import com.genius.primavera.infrastructure.security.social.github.GithubUserDetails;
import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Slf4j
@Configuration
public class PrimaveraSocialConfiguration {

	private final ObjectMapper objectMapper;
	private final PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	public PrimaveraSocialConfiguration(ObjectMapper objectMapper, PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService) {
		this.objectMapper = objectMapper;
		this.primaveraSocialUserDetailsService = primaveraSocialUserDetailsService;
	}

	@Bean
	public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
		DefaultOAuth2UserService defaultService = new DefaultOAuth2UserService();
		
		return new OAuth2UserService<OAuth2UserRequest, OAuth2User>() {
			@Override
			public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
				OAuth2User oauth2User = defaultService.loadUser(userRequest);
				String registrationId = userRequest.getClientRegistration().getRegistrationId();
				
				try {
					SocialUserDetails userDetails = convertToUserDetails(oauth2User, registrationId);
					if (userDetails != null) {
						String accessToken = userRequest.getAccessToken().getTokenValue();
						long expiration = userRequest.getAccessToken().getExpiresAt() != null 
							? userRequest.getAccessToken().getExpiresAt().toEpochMilli() 
							: System.currentTimeMillis() + 3600000;
						
						userDetails.setAccessToken(accessToken);
						userDetails.setExpiration(expiration);
						
						UserConnection userConnection = createUserConnection(userDetails, registrationId);
						primaveraSocialUserDetailsService.doAuthentication(userConnection);
					}
				} catch (Exception e) {
					log.error("Failed to process OAuth2 user: {}", e.getMessage(), e);
					throw new OAuth2AuthenticationException("Failed to process OAuth2 user");
				}
				
				return oauth2User;
			}
		};
	}

	private SocialUserDetails convertToUserDetails(OAuth2User oauth2User, String registrationId) {
		try {
			return switch (registrationId.toLowerCase()) {
				case "google" -> objectMapper.convertValue(oauth2User.getAttributes(), GoogleUserDetails.class);
				case "facebook" -> objectMapper.convertValue(oauth2User.getAttributes(), FacebookUserDetails.class);
				case "github" -> objectMapper.convertValue(oauth2User.getAttributes(), GithubUserDetails.class);
				default -> {
					log.warn("Unknown OAuth2 provider: {}", registrationId);
					yield null;
				}
			};
		} catch (Exception e) {
			log.error("Failed to convert OAuth2 user attributes for provider {}: {}", registrationId, e.getMessage());
			return null;
		}
	}

	private UserConnection createUserConnection(SocialUserDetails userDetails, String registrationId) {
		return switch (registrationId.toLowerCase()) {
			case "google" -> UserConnection.valueOf((GoogleUserDetails) userDetails);
			case "facebook" -> UserConnection.valueOf((FacebookUserDetails) userDetails);
			case "github" -> UserConnection.valueOf((GithubUserDetails) userDetails);
			default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
		};
	}
}