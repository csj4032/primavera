package com.genius.primavera.infrastructure.security.social.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Getter
public class KakaoUserDetails implements SocialUserDetails {
	private String id;
	@JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private Integer expiresIn;

	public void setAccessToken(OAuth2AccessToken accessToken) {
		this.accessToken = accessToken.getValue();
		this.expiresIn = -1;
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
