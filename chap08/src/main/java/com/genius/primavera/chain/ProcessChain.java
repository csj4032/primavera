package com.genius.primavera.chain;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ProcessChain implements Chain {
	private List<Process> processes;
	private Post post;
	private int currentPosition;
	private int size;
	private boolean validation;

	public ProcessChain(List<Process> processes, Post post) {
		this.processes = processes;
		this.post = post;
		this.size = processes.size();
		this.currentPosition = 0;
		this.validation = false;
	}

	@Override
	public void doProcess() {
		if (currentPosition == size) {
			validation = true;
			log.info("Validation Complete");
		} else {
			currentPosition++;
			Process process = processes.get(currentPosition - 1);
			process.doProcess(post, this);
		}
	}
}