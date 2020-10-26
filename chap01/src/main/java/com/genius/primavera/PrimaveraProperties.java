package com.genius.primavera;

import com.genius.primavera.domain.User;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "com.genius.primavera")
public class PrimaveraProperties {
	private String username;
	private String password;
	private String url;
	private List<String> tables;
	private Map<String, String> params;
	private List<User> users;
}