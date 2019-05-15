package com.genius.primavera.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {
    private int pageNumber;
    private int pageSize;
    private int totalSize;

    private PageRequest(int pageNumber, int pageSize, int totalSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalSize = totalSize;
    }

    public static PageRequest of() {
        return of(1, 10, 10);
    }

    public static PageRequest of(int pageNumber) {
        return of(pageNumber, 10, 10);
    }

    public static PageRequest of(int pageNumber, int pageSize) {
        return of(pageNumber, pageSize, 10);
    }

    public static PageRequest of(int pageNumber, int pageSize, int totalSize) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }

        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        if (totalSize < 1) {
            throw new IllegalArgumentException("Total size must not be less than one!");
        }

        return new PageRequest(pageNumber, pageSize, totalSize);
    }

    public int getRowNumber() {
        return getPageNumber() == 1 ? 1 : (getPageNumber() - 1) * getPageSize();
    }

    public int getOffset() {
        return getPageSize();
    }
}