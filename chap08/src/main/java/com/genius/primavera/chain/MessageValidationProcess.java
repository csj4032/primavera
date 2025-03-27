package com.genius.primavera.chain;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class MessageValidationProcess implements Process {

	private Process prev;

	@Override
	public void doProcess(Post post, Chain chain) {
		if (!post.getMessage().isEmpty()) {
			log.info("MessageValidationProcess : {}", post.getMessage());
			chain.doProcess();
		} else {
			log.info("MessageValidationProcess : Empty");
		}
	}
}
