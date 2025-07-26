package com.genius.primavera.infrastructure.security.social.google;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.genius.primavera.infrastructure.security.social.SocialUserDetails;

import lombok.Getter;

@Getter
public class GoogleUserDetails implements SocialUserDetails {
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

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
