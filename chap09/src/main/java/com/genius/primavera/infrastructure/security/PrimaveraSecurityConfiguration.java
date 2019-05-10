package com.genius.primavera.infrastructure.security;

import com.genius.primavera.infrastructure.filter.PrimaveraFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.Filter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class PrimaveraSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private AuthenticationSuccessHandler successHandler = (request, response, authentication) -> log.info("success : " + request.getContextPath());
	private AuthenticationFailureHandler failureHandler = (request, response, authentication) -> log.info("failure : " + request.getContextPath());

	private final Filter ssoFilter;
	private final PrimaveraUserDetailsService primaveraUserDetailsService;

	public PrimaveraSecurityConfiguration(Filter ssoFilter, PrimaveraUserDetailsService primaveraUserDetailsService) {
		this.ssoFilter = ssoFilter;
		this.primaveraUserDetailsService = primaveraUserDetailsService;
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		auth.inMemoryAuthentication()
				.withUser("Genius").password("{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.").roles("USER")
				.and()
				.withUser("Marcus Tullius Cicero").password(encoder.encode("password")).roles("MANAGER")
				.and()
				.withUser("Julius Caesar").password(encoder.encode("password")).roles("ADMINISTRATOR");
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	public void configure(WebSecurity webSecurity) throws Exception {
		webSecurity.ignoring().antMatchers(HttpMethod.GET, "/resources/**", "/bower_components/**", "/dist/**", "/plugins/**", "/favicon.ico");
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeRequests()
				.antMatchers("/auth/**", "/login/**", "/error").permitAll()
				.anyRequest().authenticated()
				.and()
				.addFilterBefore(new PrimaveraFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(ssoFilter, BasicAuthenticationFilter.class)
				.formLogin()
				.usernameParameter("email")
				.passwordParameter("password")
				.loginPage("/login")
				.loginProcessingUrl("/signin")
				.successHandler(successHandler)
				.defaultSuccessUrl("/index", true)
				.failureHandler(failureHandler)
				.failureUrl("/login?error=true")
				.and()
				.logout()
				.logoutUrl("/signout")
				.deleteCookies("JSESSIONID");
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(primaveraUserDetailsService);
		return authProvider;
	}
}