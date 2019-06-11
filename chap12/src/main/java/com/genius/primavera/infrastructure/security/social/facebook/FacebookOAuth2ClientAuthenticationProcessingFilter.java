package com.genius.primavera.infrastructure.security.social.facebook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.user.UserConnection;
import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FacebookOAuth2ClientAuthenticationProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

	private ObjectMapper objectMapper;
	private PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	public FacebookOAuth2ClientAuthenticationProcessingFilter(ObjectMapper objectMapper, PrimaveraSocialUserDetailsService socialService) {
		super("/login/facebook");
		this.objectMapper = objectMapper;
		this.primaveraSocialUserDetailsService = socialService;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		var userDetails = objectMapper.convertValue(((OAuth2Authentication) authResult).getUserAuthentication().getDetails(), FacebookUserDetails.class);
		userDetails.setAccessToken(restTemplate.getAccessToken());
		super.successfulAuthentication(request, response, chain, primaveraSocialUserDetailsService.doAuthentication(UserConnection.valueOf(userDetails)));
	}
}