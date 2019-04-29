package com.genius.primavera.chain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ProcessChainTest {

    private static final Post post = new Post();
    private static final List<Process> processes = new ArrayList();

    @BeforeAll
    public static void before() {
        post.setFrom("ABC");
        post.setTo("ZYX");
        post.setMessage("Message");
        Process messageValidationProcess = new MessageValidationProcess();
        Process fromValidationProcess = new FromValidationProcess();
        Process toValidationProcess = new ToValidationProcess();
        processes.add(messageValidationProcess);
        processes.add(fromValidationProcess);
        processes.add(toValidationProcess);
    }

    @Test
    public void chainTest() {
        Chain chain = new ProcessChain(processes, post);
        chain.doProcess();
    }
}