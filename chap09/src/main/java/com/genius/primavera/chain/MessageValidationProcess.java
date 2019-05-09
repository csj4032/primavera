package com.genius.primavera.chain;

public class MessageValidationProcess implements Process {

    @Override
    public void doProcess(Post post, Chain chain) {
        if (!post.getMessage().isEmpty()) {
            System.out.println("MessageValidationProcess : " + post.getMessage());
            chain.doProcess();
        } else {
            System.out.println("MessageValidationProcess : Empty");
        }
    }
}
