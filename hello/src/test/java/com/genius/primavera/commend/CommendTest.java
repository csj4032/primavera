package com.genius.primavera.commend;

import com.genius.primavera.EnumType;
import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommendTest {

	@Test
	public void commendTest() {
		List<Person> people = List.of(new Person("A", 1, EnumType.ABC), new Person("B", 1, EnumType.GHI));
		Chain chain = new MarkChain(new DrawChain(null));
		List<Commends> commends = new ArrayList();
		commends.add(new ChainCommend(chain));
		CommendManager commendManager = new CommendManager(commends);
		commendManager.append(new ValidationCommend(EnumType.ABC));
		List<Person> destination = commendManager.execute(people);
		log.info("original : {}", people);
		log.info("destination : {}", destination);
	}
}
