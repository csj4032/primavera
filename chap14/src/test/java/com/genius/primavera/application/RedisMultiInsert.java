package com.genius.primavera.application;

import com.genius.primavera.domain.model.Temp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Execution(value = ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisMultiInsert {

	@Autowired
	private RedisTemplate<String, Temp> redisTemplate;

	@Test
	@Disabled
	@RepeatedTest(5)
	public void singleInsert() {
		for (long j = 0; j < 350_000L; j++) {
			redisTemplate.opsForValue().set(String.valueOf(j), new Temp(j, LocalDateTime.now()));
		}
	}

	@Test
	@RepeatedTest(350)
	public void multiInsert() {
		Map<String, Temp> map = new HashMap<>();
		for (long j = 0; j < 10000L; j++) map.put(String.valueOf(j), new Temp(j, LocalDateTime.now()));
		redisTemplate.opsForValue().multiSet(map);
	}

	@Test
	@Disabled
	public void multiSelect() {
		List<String> list = new ArrayList<>();
		for (long i = 0; i < 1000L; i++) list.add(String.valueOf(i));
		List<Temp> temps = redisTemplate.opsForValue().multiGet(list);
		System.out.println(temps.size());
	}
}