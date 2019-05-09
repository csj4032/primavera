package com.genius.primavera.chain;

public class ToValidationProcess implements Process {

    @Override
    public void doProcess(Post post, Chain chain) {
        if (!post.getTo().isEmpty()) {
            System.out.println("ToValidationProcess : " + post.getTo());
            chain.doProcess();
        } else {
            System.out.println("ToValidationProcess : Empty");
        }
    }
}