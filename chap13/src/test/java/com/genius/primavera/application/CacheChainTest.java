package com.genius.primavera.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.application.cache.LocalCache;
import com.genius.primavera.domain.model.Temp;
import com.genius.primavera.domain.repository.TempRepository;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CacheChainTest {

	@Autowired
	private LocalCache localCache;
	@Autowired
	private RedisTemplate<String, Temp> redisTemplate;
	@Autowired
	private TempRepository tempRepository;

	@BeforeEach
	public void init() {
		LongStream.rangeClosed(1, 100).forEach(Unchecked.longConsumer(idx -> redisTemplate.delete(String.valueOf(idx))));
		LongStream.rangeClosed(1, 10).forEach(Unchecked.longConsumer(idx -> redisTemplate.opsForValue().set(String.valueOf(idx), new Temp(idx, LocalDateTime.now()))));
		Object cache = redisTemplate.execute((RedisCallback) connection -> connection.mGet("1".getBytes(), "2".getBytes(), "3".getBytes(), "4".getBytes()));
		((List<byte[]>) cache).forEach(obj -> log.info("Redis temp : {}", obj));
		((List<byte[]>) cache).forEach(obj -> log.info("Redis temp : {}", new String(obj)));
		LongStream.rangeClosed(11, 20).forEach(e -> tempRepository.save(new Temp(e, LocalDateTime.now())));
		tempRepository.findByIdIn(List.of(11L, 12L, 13L, 14L)).forEach(temp -> log.info("Mongo temp : {}", temp));
	}

	@Test
	public void cacheTest() {
		List<Optional<Temp>> temps = localCache.getTempById(List.of(1L, 2L, 6L, 7L, 8L, 11L, 12L, 13L, 14L, 15L, 21L, 22L));
		temps.forEach(e -> log.info("Temps : {}", e));
		localCache.getCache().asMap().entrySet().forEach(e -> log.info("LocalCache : {}", e));
		redisTemplate.opsForValue().multiGet(List.of("11", "12", "13", "14", "15", "21", "22")).forEach(e -> log.info("Redis temp : {}", e));
	}
}