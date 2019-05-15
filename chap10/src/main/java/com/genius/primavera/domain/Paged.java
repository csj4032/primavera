package com.genius.primavera.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paged<T> {
    private PageRequest pageRequest;
    private int totalElements;
    private int totalPages;
    private List<T> contents;

    public Paged(PageRequest pageRequest, List<T> contents, int totalElements) {
        this.pageRequest = pageRequest;
        this.contents = contents;
        this.totalElements = totalElements;
    }

    public int getPageByTotalSize() {
        int pageByTotalSize = (int) Math.ceil((double) getPageNumber() / (double) getTotalSize());
        return pageByTotalSize == 1 ? 0 : pageByTotalSize - 1;
    }

    public int getTotalPages() {
        return getTotalElements() == 0 ? 1 : (int) Math.ceil((double) getTotalElements() / (double) getPageSize());
    }

    public boolean isFirst() {
        return !hasPrevious();
    }

    public boolean isLast() {
        return !hasNext();
    }

    public Integer getFirstPagedNumber() {
        return (getPageByTotalSize() * getTotalSize()) + 1;
    }

    public Integer getLastPagedNumber() {
        return Math.min((getFirstPagedNumber() + getTotalSize() - 1), getTotalPages());
    }

    public List<Integer> getPagedNumbers() {
        return IntStream.rangeClosed(getFirstPagedNumber(), getLastPagedNumber()).boxed().collect(Collectors.toList());
    }

    public boolean hasPrevious() {
        return pageRequest.getPageNumber() > 1;
    }

    public boolean hasNext() {
        return pageRequest.getPageNumber() < getTotalPages();
    }

    public int getPageNumber() {
        return pageRequest.getPageNumber();
    }

    public int getPageSize() {
        return pageRequest.getPageSize();
    }

    private int getTotalSize() {
        return pageRequest.getTotalSize();
    }
}