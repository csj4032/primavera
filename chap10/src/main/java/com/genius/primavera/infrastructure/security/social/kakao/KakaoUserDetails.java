package com.genius.primavera.infrastructure.security.social.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserDetails implements SocialUserDetails {
	private String id;
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private long expiration;

	@Override
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public void setExpiration(long expiration) {
		this.expiration = expiration;
	}

	public String getEmail() {
		return kakaoAccount.getEmail();
	}

	public String getThumbnailImageUrl() {
		return kakaoAccount.getProfile().getThumbnailImageUrl();
	}

	public String getProfileImageUrl() {
		return kakaoAccount.getProfile().getProfileImageUrl();
	}

	public String getNickname() {
		return kakaoAccount.getProfile().getNickname();
	}

	@Getter
	@Setter
	public static class KakaoAccount {
		private Profile profile;
		private String email;
	}

	@Getter
	@Setter
	public static class Profile {
		private String nickname;
		@JsonProperty("thumbnail_image_url")
		private String thumbnailImageUrl;
		@JsonProperty("profile_image_url")
		private String profileImageUrl;
	}

}
