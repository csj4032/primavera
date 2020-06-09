package com.genius.primavera.commend;

import com.genius.primavera.Person;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ChainCommend implements Commends<Person> {

	private Chain<Person> chain;

	public ChainCommend(Chain<Person> chain) {
		this.chain = chain;
	}

	@Override
	public List<Person> execute(List<Person> entry) {
		log.info("Chain Commend");
		return chain.support(entry);
	}
}
