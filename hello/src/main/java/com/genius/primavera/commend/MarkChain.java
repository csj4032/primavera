package com.genius.primavera.commend;

import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MarkChain extends AbstractChain {

	public MarkChain(Chain next) {
		super(next);
	}

	@Override
	public List<Person> support(List<Person> list) {
		log.info("Mark Chain");
		if (next == null) return list;
		return next.support(list.stream().peek(p -> p.setAge(10)).collect(Collectors.toList()));
	}
}
