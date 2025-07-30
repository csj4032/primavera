package com.genius.primavera.domain.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ReverseEnumResolver<T extends Enum, Y> {

	private final String classCanonicalName;
	private final Map<Y, T> valueMap = new HashMap<>();

	public ReverseEnumResolver(Class<T> enumClass, Function<T, Y> toValueFunction) {
		classCanonicalName = enumClass.getCanonicalName();
		for (T t : enumClass.getEnumConstants()) {
			valueMap.put(toValueFunction.apply(t), t);
		}
	}

	public T get(Y value) {
		T enumVal = valueMap.get(value);
		if (enumVal == null) {
			throw new IllegalArgumentException("No enum constant for '" + value + "' in " + classCanonicalName);
		}
		return enumVal;
	}
}