package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class PairTest {

    @Test
    public void pairTest() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        List<String> words = List.of("A", "B", "C", "D", "E");
        List<Triplet<Integer, String, String>> triplets = numbers.stream()
                .map(n -> Pair.with(n, words.get(n - 1)))
                .map(p -> Triplet.with(p.getValue0(), p.getValue1(), p.getValue0() + p.getValue1())).collect(toList());
        log.info(triplets.toString());
    }
}