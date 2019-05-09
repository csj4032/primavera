package com.genius.primavera.infrastructure.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser("Genius").password("{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.").roles("USER")
                .and()
                .withUser("Marcus Tullius Cicero").password(encoder.encode("password")).roles("MANAGER")
                .and()
                .withUser("Julius Caesar").password(encoder.encode("password")).roles("ADMIN");
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
                .antMatchers("/login").permitAll()
                .antMatchers("/index").hasAnyRole("USER", "MANAGER", "ADMIN")
                .antMatchers("/manager").hasAnyRole("MANAGER", "ADMIN")
                .antMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/login")
                .loginProcessingUrl("/signin")
                .defaultSuccessUrl("/index", true)
                .failureUrl("/login?error=true")
                .and()
                .logout()
                .logoutUrl("/signout")
                .deleteCookies("JSESSIONID");
    }
}