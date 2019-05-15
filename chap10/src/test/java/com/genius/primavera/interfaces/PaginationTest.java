package com.genius.primavera.interfaces;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaginationTest {

    private static List<Post> posts = new ArrayList<>();

    @BeforeAll
    public static void setUp() {
        IntStream.rangeClosed(1, 95).forEach(i -> posts.add(Post.builder().id(i).subject("subject " + i).build()));
    }

    @Test
    @Order(1)
    @DisplayName("총 95 아이템 페이지 사이즈 5개 6페이지 호출")
    public void page6Size5() {
        PageRequest pageRequest = PageRequest.of(6, 5);
        List<Post> contents = posts.stream().skip(pageRequest.getRowNumber()).limit(pageRequest.getOffset()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(6, paged.getPageNumber());
        Assertions.assertEquals(5, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPagedNumbers());
        Assertions.assertEquals(5, paged.getContents().size());
        Assertions.assertEquals(26, paged.getContents().get(0).getId());
        Assertions.assertEquals(30, paged.getContents().get(4).getId());
    }

    @Test
    @Order(2)
    @DisplayName("총 95 아이템 페이지 사이즈 10개 2페이지 호출")
    public void page2Size10() {
        PageRequest pageRequest = PageRequest.of(2);
        List<Post> contents = posts.stream().skip(pageRequest.getRowNumber()).limit(pageRequest.getOffset()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(2, paged.getPageNumber());
        Assertions.assertEquals(10, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPagedNumbers());
        Assertions.assertEquals(10, paged.getContents().size());
        Assertions.assertEquals(11, paged.getContents().get(0).getId());
        Assertions.assertEquals(20, paged.getContents().get(9).getId());
    }

    @Test
    @Order(3)
    @DisplayName("총 95 아이템 페이지 사이즈 20개 3페이지 호출")
    public void page3Size20() {
        PageRequest pageRequest = PageRequest.of(3, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getRowNumber()).limit(pageRequest.getOffset()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(3, paged.getPageNumber());
        Assertions.assertEquals(20, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5), paged.getPagedNumbers());
        Assertions.assertEquals(20, paged.getContents().size());
        Assertions.assertEquals(41, paged.getContents().get(0).getId());
        Assertions.assertEquals(60, paged.getContents().get(19).getId());
    }

    @Test
    @Order(4)
    @DisplayName("총 95 아이템 페이지 사이즈 5개 페이지토탈사이즈 20 5페이지 호출")
    public void page5Size20Total20() {
        PageRequest pageRequest = PageRequest.of(5, 20, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getRowNumber()).limit(pageRequest.getOffset()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(5, paged.getPageNumber());
        Assertions.assertEquals(20, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5), paged.getPagedNumbers());
        Assertions.assertEquals(15, paged.getContents().size());
        Assertions.assertEquals(81, paged.getContents().get(0).getId());
        Assertions.assertEquals(95, paged.getContents().get(14).getId());
    }

    @Test
    @Order(5)
    @DisplayName("총 95 아이템 페이지 사이즈 5개 페이지토탈사이즈 5 5페이지 호출")
    public void page5Size20Total5() {
        PageRequest pageRequest = PageRequest.of(4, 20, 3);
        List<Post> contents = posts.stream().skip(pageRequest.getRowNumber()).limit(pageRequest.getOffset()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(4, paged.getPageNumber());
        Assertions.assertEquals(20, paged.getPageSize());
        Assertions.assertEquals(List.of(4,5), paged.getPagedNumbers());
        Assertions.assertEquals(4, paged.getFirstPagedNumber());
        Assertions.assertEquals(5, paged.getLastPagedNumber());
        Assertions.assertEquals(20, paged.getContents().size());
        Assertions.assertEquals(61, paged.getContents().get(0).getId());
        Assertions.assertEquals(80, paged.getContents().get(19).getId());
    }
}