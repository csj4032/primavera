package com.genius.primavera.commend;

import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CommendManager<E extends Commends> {

	private List<E> commends;

	public CommendManager(List<E> commends) {
		this.commends = commends;
	}

	public List<Person> execute(List<Person> people) {
		log.info("CommendManager execute");
		return onExecute(people);
	}

	// instanceOf
	// visitor
	private List<Person> onExecute(List<Person> people) {
		List<Person> result = people;
		for (Commends commend : commends) result = commend.execute(result);
		return result;
	}

	public void append(E commend) {
		commends.add(commend);
	}
}