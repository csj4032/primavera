package com.genius.primavera.infrastructure.security.social.facebook;

import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;

@Getter
public class FacebookUserDetails implements SocialUserDetails {
    private String id;
    private String email;
    private String name;
    private Instant expiration;
    private String accessToken;

    public void setAccessToken(OAuth2AccessToken accessToken) {
        this.accessToken = accessToken.getTokenValue();
        this.expiration = accessToken.getExpiresAt();
    }
}