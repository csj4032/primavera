package com.genius.primavera.infrastructure.security.social.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.genius.primavera.infrastructure.security.social.SocialUserDetails;
import lombok.Getter;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@Getter
public class GithubUserDetails implements SocialUserDetails {
	private String login;
	private String id;
	@JsonProperty("node_id")
	private String nodeId;
	@JsonProperty("avatar_url")
	private String avatarUrl;
	@JsonProperty("gravatar_id")
	private String gravatarId;
	private String url;
	@JsonProperty("html_url")
	private String htmlUrl;
	@JsonProperty("followers_url")
	private String followersUrl;
	private String name;
	private String email;
	private String bio;
	private long expiration;
	private String accessToken;

	public void setAccessToken(OAuth2AccessToken accessToken) {
		this.accessToken = accessToken.getValue();
		this.expiration = -1l;
	}
}
