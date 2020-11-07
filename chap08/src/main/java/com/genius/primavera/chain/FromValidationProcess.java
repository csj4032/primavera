package com.genius.primavera.chain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FromValidationProcess implements Process {

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