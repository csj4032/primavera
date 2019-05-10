package com.genius.primavera.infrastructure.security;

import com.genius.primavera.infrastructure.filter.PrimaveraFilter;
import com.genius.primavera.infrastructure.security.social.GoogleOAuth2ClientAuthenticationProcessingFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class PrimaveraSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private AuthenticationSuccessHandler successHandler = (request, response, authentication) -> log.info("success : " + request.getContextPath());
    private AuthenticationFailureHandler failureHandler = (request, response, authentication) -> log.info("failure : " + request.getContextPath());

    @Autowired
    private PrimaveraSocialUserDetailsService socialService;
    @Autowired
    private OAuth2ClientContext oauth2ClientContext;
    @Autowired
    private PrimaveraUserDetailsService primaveraUserDetailsService;

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
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
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

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(google(), new GoogleOAuth2ClientAuthenticationProcessingFilter(socialService)));
        filter.setFilters(filters);
        return filter;
    }

    private Filter ssoFilter(ClientResources client, OAuth2ClientAuthenticationProcessingFilter filter) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(client.getClient(),oauth2ClientContext);
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
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(primaveraUserDetailsService);
        return authProvider;
    }
}