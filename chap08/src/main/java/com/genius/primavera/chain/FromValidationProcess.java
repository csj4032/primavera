package com.genius.primavera.chain;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class FromValidationProcess implements Process {

    public FromValidationProcess(MessageValidationProcess messageValidationProcess) {
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