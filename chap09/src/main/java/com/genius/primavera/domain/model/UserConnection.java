package com.genius.primavera.domain.model;

import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserConnection {
    private long id;
    private String email;
    private ProviderType provider;
    private String providerId;
    private String displayName;
    private String profileUrl;
    private String imageUrl;
    private String accessToken;
    private long expireTime;

    public static UserConnection valueOf(GoogleUserDetails userDetails) {
        return UserConnection.builder()
                .expireTime(userDetails.getExpiration())
                .accessToken(userDetails.getAccessToken())
                .providerId(userDetails.getSub())
                .email(userDetails.getEmail())
                .displayName(userDetails.getName())
                .imageUrl(userDetails.getPicture())
                .provider(ProviderType.GOOGLE)
                .profileUrl(userDetails.getPicture())
                .build();
    }
}
