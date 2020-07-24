package com.genius.primavera.infrastructure.serializer;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.xerial.snappy.Snappy;

import java.io.Serializable;

/**
 * https://github.com/cboursinos/java-spring-redis-compression-snappy-kryo
 */
public class SnappyRedisSerializer<T> implements RedisSerializer<T> {

	private RedisSerializer<T> innerSerializer;

	public SnappyRedisSerializer(RedisSerializer<T> innerSerializer) {
		this.innerSerializer = innerSerializer;
	}

	/**
	 * Create a byte array by serialising and Compressing a java graph (object)
	 */
	@Override
	public byte[] serialize(T t) throws SerializationException {
		if (t == null) {
			return new byte[0];
		}
		try {
			byte[] bytes = innerSerializer != null ? innerSerializer.serialize(t) : SerializationUtils.serialize((Serializable) t);
			return Snappy.compress(bytes);
		} catch (Exception e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}

	/**
	 * Create a java graph (object) by uncompressing and deserializing a byte array
	 */
	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			byte[] bos = Snappy.uncompress(bytes);
			return (T) (innerSerializer != null ? innerSerializer.deserialize(bos) : SerializationUtils.deserialize(bos));
		} catch (Exception e) {
			throw new SerializationException(e.getMessage(), e);
		}
	}
}
