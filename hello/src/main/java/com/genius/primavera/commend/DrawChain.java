package com.genius.primavera.commend;

import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
public class DrawChain extends AbstractChain {

	public DrawChain(Chain next) {
		super(next);
	}

	@Override
	public List<Person> support(List<Person> list) {
		log.info("Draw Chain");
		if (next == null) return list.stream().peek(p -> p.setName("AAAA")).collect(Collectors.toList());
		return next.support(list);
	}
}
