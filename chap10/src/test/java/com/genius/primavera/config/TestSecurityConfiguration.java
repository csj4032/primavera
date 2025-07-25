package com.genius.primavera.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfiguration {

	@Bean
	@Primary
	public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authz -> authz
						.anyRequest().permitAll()
				)
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(form -> form.disable());
		
		return http.build();
	}

	@Bean
	@Primary
	public UserDetailsService testUserDetailsService() {
		var genius = new User("Genius Choi", "{noop}password", List.of(new SimpleGrantedAuthority("USER")));
		return new InMemoryUserDetailsManager(genius);
	}

	@Bean
	@Primary
	public PasswordEncoder testPasswordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}