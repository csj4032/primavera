package com.genius.primavera.commend;

import com.genius.primavera.EnumType;
import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ValidationCommend implements Commends<Person> {

	private EnumType enumType;

	public ValidationCommend() {
		this(EnumType.ABC);
	}

	public ValidationCommend(EnumType enumType) {
		this.enumType = enumType;
	}

	@Override
	public List<Person> execute(List<Person> people) {
		log.info("Validation Commend : {}", this.enumType);
		return people.stream().filter(p -> p.getEnumType().equals(enumType)).collect(Collectors.toList());
	}
}