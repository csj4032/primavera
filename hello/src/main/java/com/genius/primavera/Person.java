package com.genius.primavera;

import lombok.*;
import org.jooq.lambda.tuple.Tuple3;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Person {
	private String name;
	private Integer age;
	private EnumType enumType;

	public static Person of(Tuple3<String, Integer, EnumType> tuple3) {
		return new Person(tuple3.v1, tuple3.v2, tuple3.v3);
	}
}
