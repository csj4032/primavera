package com.genius.primavera.commend;

import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AbstractChain implements Chain<Person> {

	protected Chain next;

	public AbstractChain(Chain next) {
		this.next = next;
	}

	@Override
	public List<Person> support(List<Person> list) {
		log.info("Abstract Chain");
		return next.support(list);
	}
}
