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
    @DisplayName("총 95 아이템, 6페이지, 페이지 사이즈 5")
    public void page6Size5() {
        PageRequest pageRequest = PageRequest.of(6, 5);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(5, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPaginates());
        Assertions.assertEquals(6, paged.getPageNumber());
        Assertions.assertEquals(5, paged.getContents().size());
        Assertions.assertEquals(26, paged.getContents().get(0).getId());
        Assertions.assertEquals(30, paged.getContents().get(4).getId());
    }

    @Test
    @Order(2)
    @DisplayName("총 95 아이템, 2페이지,  페이지사이즈 10")
    public void page2Size10() {
        PageRequest pageRequest = PageRequest.of(2);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(2, paged.getPageNumber());
        Assertions.assertEquals(10, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPaginates());
        Assertions.assertEquals(10, paged.getContents().size());
        Assertions.assertEquals(11, paged.getContents().get(0).getId());
        Assertions.assertEquals(20, paged.getContents().get(9).getId());
    }

    @Test
    @Order(3)
    @DisplayName("총 95 아이템, 3페이지, 페이지사이즈 20")
    public void page3Size20() {
        PageRequest pageRequest = PageRequest.of(3, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(3, paged.getPageNumber());
        Assertions.assertEquals(20, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5), paged.getPaginates());
        Assertions.assertEquals(20, paged.getContents().size());
        Assertions.assertEquals(41, paged.getContents().get(0).getId());
        Assertions.assertEquals(60, paged.getContents().get(19).getId());
    }

    @Test
    @Order(4)
    @DisplayName("총 95 아이템,  5페이지, 페이지사이즈 5, 페이지토탈사이즈 20")
    public void page5Size20Total20() {
        PageRequest pageRequest = PageRequest.of(5, 20, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(5, paged.getPageNumber());
        Assertions.assertEquals(20, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5), paged.getPaginates());
        Assertions.assertEquals(15, paged.getContents().size());
        Assertions.assertEquals(81, paged.getContents().get(0).getId());
        Assertions.assertEquals(95, paged.getContents().get(14).getId());
    }

    @Test
    @Order(5)
    @DisplayName("총 95 아이템,  5페이지, 페이지사이즈 5, 페이지토탈사이즈 3")
    public void page4Size20Total5() {
        PageRequest pageRequest = PageRequest.of(4, 20, 3);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(4, paged.getPageNumber());
        Assertions.assertEquals(20, paged.getPageSize());
        Assertions.assertEquals(List.of(4,5), paged.getPaginates());
        Assertions.assertEquals(4, paged.getFirstPagedNumber());
        Assertions.assertEquals(5, paged.getLastPagedNumber());
        Assertions.assertEquals(20, paged.getContents().size());
        Assertions.assertEquals(61, paged.getContents().get(0).getId());
        Assertions.assertEquals(80, paged.getContents().get(19).getId());
    }

    @Test
    @Order(6)
    @DisplayName("총 95 아이템, 10페이지, 페이지사이즈 1, 페이지토탈사이즈 20")
    public void page10Size1Total20() {
        PageRequest pageRequest = PageRequest.of(10, 1, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(10, paged.getPageNumber());
        Assertions.assertEquals(1, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), paged.getPaginates());
        Assertions.assertEquals(1, paged.getFirstPagedNumber());
        Assertions.assertEquals(20, paged.getLastPagedNumber());
        Assertions.assertEquals(1, paged.getContents().size());
        Assertions.assertEquals(10, paged.getContents().get(0).getId());
        Assertions.assertEquals(10, paged.getContents().get(0).getId());
        Assertions.assertTrue(paged.hasNext());
        Assertions.assertTrue(paged.hasPrevious());
    }

    @Test
    @Order(7)
    @DisplayName("총 95 아이템, 10페이지, 페이지사이즈 10, 페이지토탈사이즈 10")
    public void page10Size10Total10() {
        PageRequest pageRequest = PageRequest.of(10, 10, 10);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        Assertions.assertEquals(10, paged.getPageNumber());
        Assertions.assertEquals(10, paged.getPageSize());
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPaginates());
        Assertions.assertEquals(1, paged.getFirstPagedNumber());
        Assertions.assertEquals(10, paged.getLastPagedNumber());
        Assertions.assertEquals(5, paged.getContents().size());
        Assertions.assertEquals(91, paged.getContents().get(0).getId());
        Assertions.assertEquals(95, paged.getContents().get(4).getId());
        Assertions.assertFalse(paged.hasNext());
        Assertions.assertTrue(paged.hasPrevious());
    }
}