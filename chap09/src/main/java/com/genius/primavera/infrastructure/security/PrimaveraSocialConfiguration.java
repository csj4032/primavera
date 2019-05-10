package com.genius.primavera.infrastructure.security;

import com.genius.primavera.infrastructure.security.social.facebook.FacebookOAuth2ClientAuthenticationProcessingFilter;
import com.genius.primavera.infrastructure.security.social.github.GithubOAuth2ClientAuthenticationProcessingFilter;
import com.genius.primavera.infrastructure.security.social.google.GoogleOAuth2ClientAuthenticationProcessingFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableOAuth2Client
public class PrimaveraSocialConfiguration {

	@Autowired
	private PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

	private final OAuth2ClientContext oauth2ClientContext;

	public PrimaveraSocialConfiguration(OAuth2ClientContext oauth2ClientContext) {
		this.oauth2ClientContext = oauth2ClientContext;
	}

	@Bean
	public Filter ssoFilter() {
		CompositeFilter filter = new CompositeFilter();
		List<Filter> filters = new ArrayList<>();
		filters.add(ssoFilter(google(), new GoogleOAuth2ClientAuthenticationProcessingFilter(primaveraSocialUserDetailsService)));
		filters.add(ssoFilter(facebook(), new FacebookOAuth2ClientAuthenticationProcessingFilter(primaveraSocialUserDetailsService)));
		filters.add(ssoFilter(github(), new GithubOAuth2ClientAuthenticationProcessingFilter(primaveraSocialUserDetailsService)));
		filter.setFilters(filters);
		return filter;
	}

	private Filter ssoFilter(ClientResources client, OAuth2ClientAuthenticationProcessingFilter filter) {
		OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
		filter.setRestTemplate(restTemplate);
		UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(), client.getClient().getClientId());
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
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
		FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}
}