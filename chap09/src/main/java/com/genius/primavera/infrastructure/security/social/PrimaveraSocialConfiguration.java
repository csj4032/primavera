package com.genius.primavera.infrastructure.security.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.UserConnection;
import com.genius.primavera.infrastructure.security.ClientResources;
import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import com.genius.primavera.infrastructure.security.social.facebook.FacebookOAuth2ClientAuthenticationProcessingFilter;
import com.genius.primavera.infrastructure.security.social.github.GithubOAuth2ClientAuthenticationProcessingFilter;
import com.genius.primavera.infrastructure.security.social.google.GoogleOAuth2ClientAuthenticationProcessingFilter;
import com.genius.primavera.infrastructure.security.social.kakao.KakaoOAuth2ClientAuthenticationProcessingFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.List;

@Slf4j
@Configuration
@EnableOAuth2Client
public class PrimaveraSocialConfiguration {

	private final ObjectMapper objectMapper;
	private final OAuth2ClientContext oauth2ClientContext;
	private final PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	public PrimaveraSocialConfiguration(ObjectMapper objectMapper, OAuth2ClientContext oauth2ClientContext, PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService) {
		this.objectMapper = objectMapper;
		this.oauth2ClientContext = oauth2ClientContext;
		this.primaveraSocialUserDetailsService = primaveraSocialUserDetailsService;
	}

	@Bean
	public Filter ssoFilter() {
		var filter = new CompositeFilter();
		var kakaoClientResources = kakao();

		filter.setFilters(List.of(
				ssoFilter(google(), new GoogleOAuth2ClientAuthenticationProcessingFilter((authResult, restTemplate, clazz) -> {
					var userDetails = objectMapper.convertValue(((OAuth2Authentication) authResult).getUserAuthentication().getDetails(), clazz);
					userDetails.setAccessToken(restTemplate.getAccessToken());
					return primaveraSocialUserDetailsService.doAuthentication(UserConnection.valueOf(userDetails));
				})),
				ssoFilter(facebook(), new FacebookOAuth2ClientAuthenticationProcessingFilter(objectMapper, primaveraSocialUserDetailsService)),
				ssoFilter(github(), new GithubOAuth2ClientAuthenticationProcessingFilter(objectMapper, primaveraSocialUserDetailsService)),
				ssoFilter(kakao(), new KakaoOAuth2ClientAuthenticationProcessingFilter(objectMapper, primaveraSocialUserDetailsService))
		));
		return filter;
	}

	private Filter ssoFilter(ClientResources client, OAuth2ClientAuthenticationProcessingFilter filter) {
		var restTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		filter.setRestTemplate(restTemplate);
		var tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId());
		filter.setTokenServices(tokenServices);
		tokenServices.setRestTemplate(restTemplate);
		return filter;
	}

	@Bean
	@ConfigurationProperties("google")
	public ClientResources google() {
		return new ClientResources();
	}

	@Bean
	@ConfigurationProperties("facebook")
	public ClientResources facebook() {
		return new ClientResources();
	}

	@Bean
	@ConfigurationProperties("github")
	public ClientResources github() {
		return new ClientResources();
	}

	@Bean
	@ConfigurationProperties("kakao")
	public ClientResources kakao() {
		return new ClientResources();
	}

	@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}
}