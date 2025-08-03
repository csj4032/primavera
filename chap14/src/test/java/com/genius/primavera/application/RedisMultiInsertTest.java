package com.genius.primavera.application;

import com.genius.primavera.domain.model.Temp;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.genius.primavera.testContainer.ContainerType.MARIADB;
import static com.genius.primavera.testContainer.ContainerType.REDIS;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Execution(value = ExecutionMode.CONCURRENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnablePrimaveraTestcontainers(containers = {REDIS, MARIADB})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisMultiInsertTest {

    @Autowired
    private RedisTemplate<String, Temp> redisTemplate;

    @RepeatedTest(5)
    public void singleInsert() {
        for (long j = 0; j < 350_000L; j++) {
            redisTemplate.opsForValue().set(String.valueOf(j), new Temp(j, Instant.now()));
        }
    }

    @RepeatedTest(350)
    public void multiInsert() {
        Map<String, Temp> map = new HashMap<>();
        for (long j = 0; j < 10000L; j++) map.put(String.valueOf(j), new Temp(j, Instant.now()));
        redisTemplate.opsForValue().multiSet(map);
    }

    @Test
    public void multiSelect() {
        List<String> list = new ArrayList<>();
        for (long i = 0; i < 1000L; i++) list.add(String.valueOf(i));
        List<Temp> temps = redisTemplate.opsForValue().multiGet(list);
        System.out.println(temps.size());
    }
}