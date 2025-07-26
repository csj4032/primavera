package com.genius.primavera.infrastructure.security.social.facebook;

import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Getter
public class FacebookUserDetails implements SocialUserDetails {
    private String id;
    private String email;
    private String name;
    private long expiration;
    private String accessToken;

    public void setAccessToken(OAuth2AccessToken accessToken) {
        this.accessToken = accessToken.getValue();
        this.expiration = accessToken.getExpiration().getTime();
    }
}
