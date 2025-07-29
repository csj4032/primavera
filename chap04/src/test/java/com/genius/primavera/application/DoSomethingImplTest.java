package com.genius.primavera.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class DoSomethingImplTest {

    @Test
    @DisplayName("DoSomethingImpl 테스트")
    public void testDoSomething() {
        DoSomething doSomething = new DoSomethingImpl();
        String result1 = doSomething.doSomething("Hello");
        assertEquals("Hello Something Something", result1, "Single argument should return the same string");
        String result2 = doSomething.doSomething("Hello", "World");
        assertEquals("Hello World Something", result2, "Two arguments should concatenate with a space");
        String result3 = doSomething.doSomething("Hello", "World", "!");
        assertEquals("Hello World ! Something", result3, "Three arguments should concatenate with spaces");
    }
}