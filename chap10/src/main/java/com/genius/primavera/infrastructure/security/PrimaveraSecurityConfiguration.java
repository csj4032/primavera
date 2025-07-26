package com.genius.primavera.infrastructure.security;

import com.genius.primavera.infrastructure.filter.PrimaveraFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.Filter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class PrimaveraSecurityConfiguration {

    private AuthenticationSuccessHandler successHandler = (request, response, authentication) -> log.info("success : " + request.getContextPath());
    private AuthenticationFailureHandler failureHandler = (request, response, authentication) -> log.info("failure : " + request.getContextPath());

    private final PrimaveraUserDetailsService primaveraUserDetailsService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;

    public PrimaveraSecurityConfiguration(PrimaveraUserDetailsService primaveraUserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService) {
        this.primaveraUserDetailsService = primaveraUserDetailsService;
        this.oauth2UserService = oauth2UserService;
    }

    @Bean
    public UserDetailsService inMemoryUserDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserDetails genius = User.withUsername("Genius")
                .password("{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.")
                .roles("USER")
                .build();
        UserDetails marcus = User.withUsername("Marcus Tullius Cicero")
                .password(encoder.encode("password"))
                .roles("MANAGER")
                .build();
        UserDetails julius = User.withUsername("Julius Caesar")
                .password(encoder.encode("password"))
                .roles("ADMINISTRATOR")
                .build();
        return new InMemoryUserDetailsManager(genius, marcus, julius);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/resources/**", "/bower_components/**", "/dist/**", "/plugins/**", "/favicon.ico").permitAll()
                        .requestMatchers("/auth/**", "/login/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new PrimaveraFilter(), UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/index", true)
                        .failureUrl("/login?error=true")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService)
                        )
                )
                .formLogin(form -> form
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginPage("/login")
                        .loginProcessingUrl("/signin")
                        .successHandler(successHandler)
                        .defaultSuccessUrl("/index", true)
                        .failureHandler(failureHandler)
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/signout")
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(primaveraUserDetailsService);
        return authProvider;
    }
}