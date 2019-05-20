package com.genius.primavera.interfaces;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleTest {

    @Test
    public void page10Size10Total10() {
        String a = IntStream.rangeClosed(0, 2).mapToObj(e -> "--").collect(Collectors.joining());
        System.out.println(a);
    }
}