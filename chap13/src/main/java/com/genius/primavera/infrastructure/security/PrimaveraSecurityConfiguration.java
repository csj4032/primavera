package com.genius.primavera.infrastructure.security;

import com.genius.primavera.infrastructure.filter.PrimaveraFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class PrimaveraSecurityConfiguration {

	private AuthenticationSuccessHandler successHandler = (request, response, authentication) -> log.info("success : " + request.getContextPath());
	private AuthenticationFailureHandler failureHandler = (request, response, authentication) -> log.info("failure : " + request.getContextPath());

	private final PrimaveraUserDetailsService primaveraUserDetailsService;

	public PrimaveraSecurityConfiguration(PrimaveraUserDetailsService primaveraUserDetailsService) {
		this.primaveraUserDetailsService = primaveraUserDetailsService;
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsManager() {
		var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		var genius = new User("Genius", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("USER")));
		var marcus = new User("Marcus Tullius Cicero", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("USER")));
		var julius = new User("Gaius Julius Caesar", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ADMIN")));
		var tiberius = new User("Tiberius", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("ADMIN")));
		return new InMemoryUserDetailsManager(genius, marcus, julius, tiberius);
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(primaveraUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authz -> authz
				.requestMatchers("/login**", "/favicon**", "/bower_components/**", "/dist/**", "/plugins/**", "/error**").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/articles/**").hasAuthority("ADMIN")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("email")
				.passwordParameter("password")
				.successHandler(successHandler)
				.failureHandler(failureHandler)
			)
			.logout(logout -> logout.permitAll())
			.authenticationProvider(authenticationProvider())
			.addFilterBefore(new PrimaveraFilter(), BasicAuthenticationFilter.class);
			// Removed SSO filter temporarily as it uses deprecated OAuth2 classes
			// .addFilterBefore(ssoFilter, BasicAuthenticationFilter.class);
		
		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}