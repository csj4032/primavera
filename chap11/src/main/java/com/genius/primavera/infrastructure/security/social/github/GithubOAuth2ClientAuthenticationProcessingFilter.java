package com.genius.primavera.infrastructure.security.social.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.user.UserConnection;
import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class GithubOAuth2ClientAuthenticationProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

	private final ObjectMapper objectMapper;
	private final PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	public GithubOAuth2ClientAuthenticationProcessingFilter(ObjectMapper objectMapper, PrimaveraSocialUserDetailsService socialService) {
		super("/login/github");
		this.objectMapper = objectMapper;
		this.primaveraSocialUserDetailsService = socialService;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		var userDetails = objectMapper.convertValue(((OAuth2Authentication) authResult).getUserAuthentication().getDetails(), GithubUserDetails.class);
		userDetails.setAccessToken(restTemplate.getAccessToken());
		super.successfulAuthentication(request, response, chain, primaveraSocialUserDetailsService.doAuthentication(UserConnection.valueOf(userDetails)));
	}
}