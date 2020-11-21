package com.genius.primavera.chain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//엘리스
public class FromValidationProcess implements Process {

	private Process prev;

	public FromValidationProcess(MessageValidationProcess messageValidationProcess) {
		//밥
		messageValidationProcess.setPrev(this);
	}

	@Override
	public void doProcess(Post post, Chain chain) {
		if (!post.getFrom().isEmpty()) {
			log.info("FromValidationProcess : {}", post.getFrom());
			chain.doProcess();
		} else {
			log.info("FromValidationProcess : Empty");
		}
	}
}