package com.genius.primavera.infrastructure.security.social.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Getter
public class KakaoUserDetails implements SocialUserDetails {
	private String id;
	private String email;
	private String nickname;
	@JsonProperty("profile_image")
	private String profileImage;
	@JsonProperty("thumbnail_image")
	private String thumbnailImage;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private Integer expiresIn;

	public void setAccessToken(OAuth2AccessToken accessToken) {
		this.accessToken = accessToken.getValue();
		this.expiresIn = -1;
	}
}
