package com.genius.primavera.domain.model.user;

import com.genius.primavera.domain.converter.ProviderTypeAttributeConverter;
import com.genius.primavera.domain.model.BaseEntity;
// Legacy OAuth2 social imports - disabled
// import com.genius.primavera.infrastructure.security.social.facebook.FacebookUserDetails;
// import com.genius.primavera.infrastructure.security.social.github.GithubUserDetails;
// import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.Audited;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "USER_CONNECTION")
@Audited(targetAuditMode = NOT_AUDITED)
public class UserConnection extends BaseEntity {

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

    // Legacy OAuth2 social methods - disabled
    /*
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
    */
}
