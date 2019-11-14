package com.genius.primavera;

import lombok.Getter;

@Getter
public enum CacheKeyPrefixType {
	PRODUCT("product", (suffix) -> "product::" + suffix),
	GROUP("group", (suffix) -> "dealInfo::" + suffix);

	private String prefix;
	private CacheKeyGenerator cacheGenerator;

	CacheKeyPrefixType(String prefix, CacheKeyGenerator cacheGenerator) {
		this.prefix = prefix;
		this.cacheGenerator = cacheGenerator;
	}
}
