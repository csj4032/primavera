package com.genius.primavera.infrastructure.security.social.kakao;

import com.genius.primavera.infrastructure.security.social.SocialAuthentication;
import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class KakaoOAuth2ClientAuthenticationProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

	private SocialAuthentication socialAuthentication;

	public KakaoOAuth2ClientAuthenticationProcessingFilter(SocialAuthentication<GoogleUserDetails> socialAuthentication) {
		super("/login/kakao");
		this.socialAuthentication = socialAuthentication;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		var usernamePasswordAuthenticationToken = socialAuthentication.getAuthentication(authResult, restTemplate, GoogleUserDetails.class);
		super.successfulAuthentication(request, response, chain, usernamePasswordAuthenticationToken);
	}
}
