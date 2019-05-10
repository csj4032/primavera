package com.genius.primavera.infrastructure.security.social.facebook;

import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FacebookOAuth2ClientAuthenticationProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

	private PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	public FacebookOAuth2ClientAuthenticationProcessingFilter(PrimaveraSocialUserDetailsService socialService) {
		super("/login/facebook");
		this.primaveraSocialUserDetailsService = socialService;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

	}
}