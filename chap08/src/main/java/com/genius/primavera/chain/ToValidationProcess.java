package com.genius.primavera.chain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ToValidationProcess implements Process {

	@Override
	public void doProcess(Post post, Chain chain) {
		if (!post.getTo().isEmpty()) {
			log.info("ToValidationProcess : {}", post.getTo());
			chain.doProcess();
		} else {
			log.info("ToValidationProcess : Empty");
		}
	}
}