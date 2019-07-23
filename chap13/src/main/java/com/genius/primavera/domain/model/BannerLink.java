package com.genius.primavera.domain.model;

import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.genius.primavera.domain.model.BannerLink.Device.MOBILE;
import static com.genius.primavera.domain.model.BannerLink.Device.PC;

@Getter
@ToString
public enum BannerLink {

	EVENT(MOBILE, PC),
	PROMOTION(MOBILE),
	PRODUCT(PC),
	DEAL(PC),
	NONE;

	private Device[] device;

	BannerLink(Device... device) {
		this.device = device;
	}

	enum Device {
		PC("http://www.naver.com"),
		MOBILE("http://m.naver.com");
		String url;

		Device(String url) {
			this.url = url;
		}
	}

	public static String valueOf(BannerLink bannerLink, Device device) {
		return BannerMap.get(bannerLink).get(device);
	}

	private static final Collector<Device, ?, Map<Device, String>> deviceCollector = Collectors.toMap(k -> k, e -> e.url);
	private static final Collector<BannerLink, ?, Map<BannerLink, Map<Device, String>>> bannerCollector = Collectors.toMap(k -> k, v -> Stream.of(v.device).collect(deviceCollector), (x, y) -> y);
	private static final Map<BannerLink, Map<Device, String>> BannerMap = Stream.of(BannerLink.values()).collect(bannerCollector);
}
