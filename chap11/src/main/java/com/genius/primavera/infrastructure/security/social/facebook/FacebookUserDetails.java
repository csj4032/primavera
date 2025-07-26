package com.genius.primavera.infrastructure.security.social.facebook;

import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;

@Getter
public class FacebookUserDetails implements SocialUserDetails {
    private String id;
    private String email;
    private String name;
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
