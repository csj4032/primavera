package com.genius.primavera.domain.converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.AttributeConverter;

public abstract class EnumAttributeConverter<X extends ConvertedEnum<Y>, Y> implements AttributeConverter<X, Y> {

	@Override
	public final Y convertToDatabaseColumn(X x) {
		return x.toDbValue();
	}

	protected abstract Class<X> enumClass();

	@Override
	public X convertToEntityAttribute(Y dbValue) {
		try {
			Method method = enumClass().getMethod("fromDbValue", dbValue.getClass());
			return (X) method.invoke(null, dbValue);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalArgumentException("...this really doesn't make sense", e);
		}
	}
}