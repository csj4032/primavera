package com.genius.primavera.infrastructure.security.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.UserConnection;
import com.genius.primavera.infrastructure.security.PrimaveraSocialUserDetailsService;
import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleOAuth2ClientAuthenticationProcessingFilter extends OAuth2ClientAuthenticationProcessingFilter {

    private ObjectMapper mapper = new ObjectMapper();
    private PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService;

    public GoogleOAuth2ClientAuthenticationProcessingFilter(PrimaveraSocialUserDetailsService primaveraSocialUserDetailsService) {
        super("/login/google");
        this.primaveraSocialUserDetailsService = primaveraSocialUserDetailsService;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        final OAuth2AccessToken accessToken = restTemplate.getAccessToken();
        final OAuth2Authentication auth = (OAuth2Authentication) authResult;
        final Object details = auth.getUserAuthentication().getDetails();
        final GoogleUserDetails userDetails = mapper.convertValue(details, GoogleUserDetails.class);
        userDetails.setAccessToken(accessToken);
        final UserConnection userConnection = UserConnection.valueOf(userDetails);
        final UsernamePasswordAuthenticationToken authenticationToken = primaveraSocialUserDetailsService.doAuthentication(userConnection);
        super.successfulAuthentication(request, response, chain, authenticationToken);
    }
}