
package com.nhncorp.lucy.security.xss.listener;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContentTypeCacheRepo {
	private static final int HARD_CACHE_CAPACITY = 1000;

	@SuppressWarnings("serial")
	private final HashMap<String, ContentType> sHardBitmapCache = new LinkedHashMap<String, ContentType>(HARD_CACHE_CAPACITY * 4 / 3 + 1, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, ContentType> eldest) {
			if (size() > HARD_CACHE_CAPACITY) {

				sSoftBitmapCache.put(eldest.getKey(), new WeakReference<ContentType>(eldest.getValue()));

				return true;
			} else {
				return false;
			}
		}
	};

	private final static ConcurrentHashMap<String, WeakReference<ContentType>> sSoftBitmapCache = new ConcurrentHashMap<String, WeakReference<ContentType>>(HARD_CACHE_CAPACITY / 2);

	private ContentType getContentTypeCacheFromCache(String url) {

		synchronized (sHardBitmapCache) {
			final ContentType contentTypeCache = sHardBitmapCache.get(url);
			if (contentTypeCache != null) {
				return contentTypeCache;
			}
		}

		WeakReference<ContentType> contentTypeCacheReference = sSoftBitmapCache.get(url);
		if (contentTypeCacheReference != null) {
			final ContentType contentTypeCache = contentTypeCacheReference.get();
			if (contentTypeCache != null) {

				return contentTypeCache;
			} else {

				sSoftBitmapCache.remove(url);
			}
		}

		return null;
	}

	public String getContentTypeFromCache(String url) {
		ContentType contentTypeCache = getContentTypeCacheFromCache(url);
		if (contentTypeCache == null) {
			return "";
		}

		Date regdate = contentTypeCache.getRegdate();
		Date today = new Date();
		String contentType = "";

		if ((today.getTime() - regdate.getTime()) < 1000 * 3600 * 24) {
			contentType = contentTypeCache.getContentType();
		} else {
			synchronized (sHardBitmapCache) {
				sHardBitmapCache.remove(url);
			}
		}
		return contentType;
	}

	public void addContentTypeToCache(String url, ContentType contentTypeCache) {
		if (contentTypeCache != null) {
			synchronized (sHardBitmapCache) {
				sHardBitmapCache.put(url, contentTypeCache);
			}
		}
	}

	public void clearCache() {
		sHardBitmapCache.clear();
		sSoftBitmapCache.clear();
	}
}
