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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class PrimaveraSecurityConfiguration {

	private final AuthenticationSuccessHandler successHandler = (request, response, authentication) -> {
		log.info("success : " + request.getContextPath());
		response.sendRedirect("/index");
	};
	
	private final AuthenticationFailureHandler failureHandler = (request, response, authentication) -> {
		log.info("failure : " + request.getContextPath());
		response.sendRedirect("/login?error=true");
	};

	private final PrimaveraUserDetailsService primaveraUserDetailsService;
	
	@Autowired(required = false)
	private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
	
	@Autowired(required = false)
	private ClientRegistrationRepository clientRegistrationRepository;

	public PrimaveraSecurityConfiguration(PrimaveraUserDetailsService primaveraUserDetailsService) {
		this.primaveraUserDetailsService = primaveraUserDetailsService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		var httpConfig = http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authz -> authz
						.requestMatchers(HttpMethod.GET, "/resources/**", "/bower_components/**", "/dist/**", "/plugins/**", "/favicon.ico").permitAll()
						.requestMatchers("/auth/**", "/login/**", "/error", "/oauth2/**").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(new PrimaveraFilter(), UsernamePasswordAuthenticationFilter.class)
				.formLogin(form -> form
						.usernameParameter("email")
						.passwordParameter("password")
						.loginPage("/login")
						.loginProcessingUrl("/signin")
						.successHandler(successHandler)
						.defaultSuccessUrl("/index", true)
						.failureHandler(failureHandler)
						.failureUrl("/login?error=true")
				);

		if (oauth2UserService != null && clientRegistrationRepository != null) {
			httpConfig.oauth2Login(oauth2 -> oauth2
					.loginPage("/login")
					.userInfoEndpoint(userInfo -> userInfo
							.userService(oauth2UserService)
					)
					.successHandler(successHandler)
					.failureHandler(failureHandler)
			);
		}
		
		httpConfig.logout(logout -> logout
				.logoutUrl("/signout")
				.deleteCookies("JSESSIONID")
		);
		
		return httpConfig.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		var genius = new User("Genius", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("USER")));
		var marcus = new User("Marcus Tullius Cicero", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("USER")));
		var julius = new User("Julius Caesar", "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.", List.of(new SimpleGrantedAuthority("USER")));
		return new InMemoryUserDetailsManager(genius, marcus, julius);
	}

	@Bean
	@SuppressWarnings("deprecation")
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(primaveraUserDetailsService);
		return authProvider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
}