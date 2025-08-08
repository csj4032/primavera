package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.jupiter.api.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@DisplayName("Java Tuples Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JavaTuplesTest {

    @Test
    @Order(1)
    @DisplayName("Pair Test")
    public void pairTest() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        List<String> words = List.of("A", "B", "C", "D", "E");
        List<Triplet<Integer, String, String>> triplets = numbers.stream()
                .map(n -> Pair.with(n, words.get(n - 1)))
                .map(p -> Triplet.with(p.getValue0(), p.getValue1(), p.getValue0() + p.getValue1())).collect(toList());
        Assertions.assertEquals(5, triplets.size());
        Assertions.assertEquals(Triplet.with(1, "A", "1A"), triplets.get(0));
        Assertions.assertEquals(Triplet.with(2, "B", "2B"), triplets.get(1));
        Assertions.assertEquals(Triplet.with(3, "C", "3C"), triplets.get(2));
        Assertions.assertEquals(Triplet.with(4, "D", "4D"), triplets.get(3));
        Assertions.assertEquals(Triplet.with(5, "E", "5E"), triplets.get(4));
    }

    @Test
    @Order(2)
    @DisplayName("Triplet Test")
    public void tripletTest() {
        List<Triplet<Integer, String, String>> triplets = List.of(
                Triplet.with(1, "A", "1A"),
                Triplet.with(2, "B", "2B"),
                Triplet.with(3, "C", "3C")
        );
        Assertions.assertEquals(3, triplets.size());
        Assertions.assertEquals(Triplet.with(1, "A", "1A"), triplets.get(0));
        Assertions.assertEquals(Triplet.with(2, "B", "2B"), triplets.get(1));
        Assertions.assertEquals(Triplet.with(3, "C", "3C"), triplets.get(2));
    }

    @Test
    @Order(3)
    @DisplayName("Pair and Triplet Combined Test")
    public void pairAndTripletCombinedTest() {
        List<Pair<Integer, String>> pairs = List.of(
                Pair.with(1, "A"),
                Pair.with(2, "B"),
                Pair.with(3, "C")
        );
        List<Triplet<Integer, String, String>> triplets = pairs.stream()
                .map(p -> Triplet.with(p.getValue0(), p.getValue1(), p.getValue0() + p.getValue1()))
                .collect(toList());

        Assertions.assertEquals(3, triplets.size());
        Assertions.assertEquals(Triplet.with(1, "A", "1A"), triplets.get(0));
        Assertions.assertEquals(Triplet.with(2, "B", "2B"), triplets.get(1));
        Assertions.assertEquals(Triplet.with(3, "C", "3C"), triplets.get(2));
    }
}