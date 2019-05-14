package com.genius.primavera.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pagination<T> {
    private int pageNumber;
    private int pageSize = 10;
    private int totalElements;
    private int totalPages = 10;
    private List<T> contents;

    public List<Integer> getPageNumber() {
        int firstPage = ((pageNumber * pageSize) / pageSize) + 1;
        int lastPage = firstPage + totalPages;
        return IntStream.rangeClosed(firstPage, lastPage).boxed().collect(Collectors.toList());
    }
}
