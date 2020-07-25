package com.genius.primavera.application.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.domain.model.Temp;
import com.genius.primavera.domain.repository.TempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCache {

	private final TempRepository tempTempRepository;
	private final RedisTemplate<String, Temp> redisTemplate;
	private final ObjectMapper objectMapper;

	public Optional<Temp> getTempById(Long id) throws JsonProcessingException {
		return Optional.of(redisTemplate.opsForValue().get(String.valueOf(id)));
	}

	public Map<Long, Optional<Temp>> getTempByIdIn(List<Long> ids) {
		List<Temp> redisTemps = getRedisTemps(ids);
		List<Long> nonRedisTempIds = getNonTempIds(ids, redisTemps);
		List<Temp> mongoTemps = tempTempRepository.findByIdIn(nonRedisTempIds);
		List<Temp> redisAndMongoTemps = Seq.seq(redisTemps).concat(Seq.seq(mongoTemps)).toList();
		List<Tuple2<Long, Temp>> idsAndTemps = Seq.seq(ids).leftOuterJoin(Seq.seq(redisAndMongoTemps), (a, b) -> a.equals(b.getId())).toList();
		List<Tuple2<Long, Temp>> redisSetTemps = Seq.seq(idsAndTemps).removeAll(Seq.seq(redisTemps.stream().map(e -> new Tuple2<>(e.getId(), e)))).toList();
		setRedis(redisSetTemps);
		return idsAndTemps.stream().collect(HashMap::new, (m, v) -> m.put(v.v1, Optional.ofNullable(v.v2)), HashMap::putAll);
	}

	private List<Long> getNonTempIds(List<Long> ids, List<Temp> temps) {
		return Seq.seq(ids)
				.leftOuterJoin(temps.stream(), (a, b) -> a.equals(b.getId()))
				.filter(t -> t.v2 == null)
				.collect(ArrayList::new, (a, t) -> a.add(t.v1), List::addAll);
	}

	private List<Temp> getRedisTemps(List<Long> ids) {
		return redisTemplate.opsForValue()
				.multiGet(ids.stream().map(String::valueOf).collect(toList()))
				.stream()
				.filter(Objects::nonNull)
				.collect(toList());
	}

	private void setRedis(List<Tuple2<Long, Temp>> redisSetTemps) {
		redisTemplate.opsForValue().multiSet(redisSetTemps.stream().collect(HashMap::new, (m, t) -> m.put(String.valueOf(t.v1()), t.v2()), HashMap::putAll));
	}
}
