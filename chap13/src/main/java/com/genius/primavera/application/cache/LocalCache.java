package com.genius.primavera.application.cache;

import com.genius.primavera.domain.model.Temp;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocalCache {

	private Cache<Long, Optional<Temp>> cache;
	private final RedisCache redisCache;

	@PostConstruct
	public void init() {
		cache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(100).build((id) -> getTempById(id));
		cache.asMap().put(1L, Optional.of(new Temp(1L, LocalDateTime.now())));
		cache.asMap().put(2L, Optional.of(new Temp(2L, LocalDateTime.now())));
		cache.asMap().put(3L, Optional.of(new Temp(3L, LocalDateTime.now())));
		cache.asMap().put(4L, Optional.of(new Temp(4L, LocalDateTime.now())));
		cache.asMap().put(5L, Optional.of(new Temp(5L, LocalDateTime.now())));
	}

	public Optional<Temp> getTempById(Long id) {
		return cache.getIfPresent(id);
	}

	public List<Optional<Temp>> getTempById(List<Long> ids) {
		Map<Long, Optional<Temp>> redisTemp = redisCache.getTempByIdIn(ids.stream().filter(id -> !cache.asMap().containsKey(id)).collect(Collectors.toList()));
		redisTemp.putAll(cache.getAllPresent(ids));
		cache.asMap().putAll(redisTemp);
		return new ArrayList<>(redisTemp.values());
	}

	public Cache<Long, Optional<Temp>> getCache() {
		return cache;
	}
}
