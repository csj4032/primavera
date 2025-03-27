package com.genius.primavera.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class PrimaveraPasswordEncoder implements PasswordEncoder {

	private static final String PREFIX = "*";

	private static final PasswordEncoder INSTANCE = new PrimaveraPasswordEncoder();

	public static PasswordEncoder getInstance() {
		return INSTANCE;
	}

	private PrimaveraPasswordEncoder() {
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return digest(rawPassword.toString());
	}

	private String digest(String rawPassword) {
		return PREFIX + DigestUtils.sha1Hex(DigestUtils.sha1(rawPassword)).toUpperCase();
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (encodedPassword == null || encodedPassword.isEmpty()) {
			log.warn("Empty encoded password");
			return false;
		}
		return isEqual(digest(rawPassword.toString()), encodedPassword);
	}

	private boolean isEqual(String rawPassword, String encodedPassword) {
		return rawPassword.equals(encodedPassword);
	}
}
