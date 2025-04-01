package com.genius.primavera.infrastructure.security.social.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.UserConnection;
import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class GithubOAuth2ClientAuthenticationProcessingFilter extends OAuth2LoginAuthenticationFilter {

	private final ObjectMapper objectMapper;
	private final PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	public GithubOAuth2ClientAuthenticationProcessingFilter(ObjectMapper objectMapper, PrimaveraSocialUserDetailsService socialService) {
		this.objectMapper = objectMapper;
		this.primaveraSocialUserDetailsService = socialService;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		final GithubUserDetails userDetails = objectMapper.convertValue(((OAuth2Authentication) authResult).getUserAuthentication().getDetails(), GithubUserDetails.class);
		userDetails.setAccessToken(restTemplate.getAccessToken());
		super.successfulAuthentication(request, response, chain, primaveraSocialUserDetailsService.doAuthentication(UserConnection.valueOf(userDetails)));
	}
}