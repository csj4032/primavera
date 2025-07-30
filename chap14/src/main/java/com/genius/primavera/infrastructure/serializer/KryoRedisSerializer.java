package com.genius.primavera.infrastructure.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class KryoRedisSerializer<T> implements RedisSerializer<T> {

	@Override
	public byte[] serialize(T t) throws SerializationException {
		return KryoSerializer.serialize(t);
	}

	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
		return KryoSerializer.deserialize(bytes);
	}
}
