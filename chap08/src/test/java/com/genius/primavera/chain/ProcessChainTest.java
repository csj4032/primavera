package com.genius.primavera.chain;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ProcessChainTest {

	@Test
	public void chainTest() {
		List<Process> processes = List.of(new FromValidationProcess(), new MessageValidationProcess(), new ToValidationProcess());
		final Post post = new Post("to", "from", "message", 1);
		final ProcessChain processChain = new ProcessChain(processes, post);
		processChain.doProcess();
	}
}