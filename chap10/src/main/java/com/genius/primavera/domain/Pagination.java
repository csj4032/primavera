package com.genius.primavera.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pagination<T> {
	private int pageNumber = 1;
	private int pageSize = 10;
	private int totalElements;
	private int totalPages;
	private int totalSize = 10;
	private int pageTotal = 1;
	private List<T> contents;

	public List<Integer> getPagesNumber() {
		int firstPage = (getPageByTotalSize() * totalSize) + 1;
		int lastPage = Math.min((firstPage + totalSize - 1), getTotalPages());
		return IntStream.rangeClosed(firstPage, lastPage).boxed().collect(Collectors.toList());
	}

	public int getPageByTotalSize() {
		int pageByTotalSize = (int) Math.ceil((double) pageNumber / (double) totalSize);
		return pageByTotalSize == 1 ? 0 : pageByTotalSize - 1;
	}

	public int getTotalPages() {
		return getTotalElements() == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) pageSize);
	}

	public boolean isFirst() {
		return !hasPrevious();
	}

	public boolean isLast() {
		return !hasNext();
	}

	public boolean hasPrevious() {
		return getPageNumber() > 1;
	}

	public boolean hasNext() {
		return getPageNumber() < getTotalPages();
	}
}