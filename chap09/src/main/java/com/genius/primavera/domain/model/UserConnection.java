package com.genius.primavera.domain.model;

import com.genius.primavera.infrastructure.security.social.facebook.FacebookUserDetails;
import com.genius.primavera.infrastructure.security.social.github.GithubUserDetails;
import com.genius.primavera.infrastructure.security.social.google.GoogleUserDetails;

import com.genius.primavera.infrastructure.security.social.kakao.KakaoUserDetails;
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

	public static UserConnection valueOf(KakaoUserDetails userDetails) {
		return UserConnection.builder()
				.expireTime(userDetails.getExpiresIn())
				.providerId(userDetails.getId())
				.accessToken(userDetails.getAccessToken())
				.email(userDetails.getEmail())
				.displayName(userDetails.getNickname())
				.imageUrl(userDetails.getThumbnailImageUrl())
				.provider(ProviderType.KAKAO)
				.profileUrl(userDetails.getProfileImageUrl())
				.build();
	}
}
