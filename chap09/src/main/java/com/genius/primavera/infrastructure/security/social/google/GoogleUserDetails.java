package com.genius.primavera.infrastructure.security.social.google;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

import lombok.Getter;

@Getter
public class GoogleUserDetails {
    private String sub;
    private String name;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    private String profile;
    private String picture;
    private String email;
    @JsonProperty("email_verified")
    private boolean emailVerified;
    private String gender;
    private String locale;
    private long expiration;
    private String accessToken;

    public void setAccessToken(OAuth2AccessToken accessToken) {
        this.accessToken = accessToken.getValue();
        this.expiration = accessToken.getExpiration().getTime();
    }
}
