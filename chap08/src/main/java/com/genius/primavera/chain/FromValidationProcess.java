package com.genius.primavera.chain;

public class FromValidationProcess implements Process {

    @Override
    public void doProcess(Post post, Chain chain) {
        if (!post.getFrom().isEmpty()) {
            System.out.println("FromValidationProcess : " + post.getFrom());
            chain.doProcess();
        } else {
            System.out.println("FromValidationProcess : Empty");
        }
    }
}