package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.ProviderTypeAttributeConverter;
import com.genius.primavera.infrastructure.security.social.facebook.FacebookUserDetails;
import com.genius.primavera.infrastructure.security.social.github.GithubUserDetails;
import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Entity
@Table(name = "USER_CONNECTION")
public class UserConnection {

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PROVIDER")
    @Convert(converter = ProviderTypeAttributeConverter.class)
    private ProviderType provider;

    @Column(name = "PROVIDER_ID")
    private String providerId;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "PROFILE_URL")
    private String profileUrl;

    @Column(name = "IMAGE_URL")
    private String imageUrl;

    @Column(name = "ACCESS_TOKEN")
    private String accessToken;

    @Column(name = "EXPIRE_TIME")
    private long expireTime;

    public static UserConnection valueOf(FacebookUserDetails userDetails) {
        return UserConnection.builder()
                .expireTime(userDetails.getExpiration())
                .accessToken(userDetails.getAccessToken())
                .providerId(userDetails.getId())
                .provider(ProviderType.FACEBOOK)
                .email(userDetails.getEmail())
                .displayName(userDetails.getName())
                .imageUrl("https://graph.facebook.com/" + userDetails.getId() + "/picture?type=large&redirect=true")
                .profileUrl("")
                .build();
    }

    public static UserConnection valueOf(GithubUserDetails userDetails) {
        return UserConnection.builder()
                .expireTime(userDetails.getExpiration())
                .accessToken(userDetails.getAccessToken())
                .providerId(userDetails.getId())
                .email(userDetails.getEmail())
                .displayName(userDetails.getName())
                .imageUrl(userDetails.getAvatarUrl())
                .provider(ProviderType.GITHUB)
                .profileUrl("")
                .build();
    }

    public static UserConnection valueOf(GoogleUserDetails userDetails) {
        return UserConnection.builder()
                .expireTime(userDetails.getExpiration())
                .accessToken(userDetails.getAccessToken())
                .providerId(userDetails.getSub())
                .email(userDetails.getEmail())
                .displayName(userDetails.getName())
                .imageUrl(userDetails.getPicture())
                .provider(ProviderType.GOOGLE)
                .profileUrl(userDetails.getProfile())
                .build();
    }
}
