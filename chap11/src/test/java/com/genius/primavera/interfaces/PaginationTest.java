package com.genius.primavera.interfaces;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.model.post.Post;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(5, paged.getPageSize());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPaginates());
        assertEquals(6, paged.getPageNumber());
        assertEquals(5, paged.getContents().size());
        assertEquals(26, paged.getContents().get(0).getId());
        assertEquals(30, paged.getContents().get(4).getId());
    }

    @Test
    @Order(2)
    @DisplayName("총 95 아이템, 2페이지,  페이지사이즈 10")
    public void page2Size10() {
        PageRequest pageRequest = PageRequest.of(2);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        assertEquals(2, paged.getPageNumber());
        assertEquals(10, paged.getPageSize());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPaginates());
        assertEquals(10, paged.getContents().size());
        assertEquals(11, paged.getContents().get(0).getId());
        assertEquals(20, paged.getContents().get(9).getId());
    }

    @Test
    @Order(3)
    @DisplayName("총 95 아이템, 3페이지, 페이지사이즈 20")
    public void page3Size20() {
        PageRequest pageRequest = PageRequest.of(3, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        assertEquals(3, paged.getPageNumber());
        assertEquals(20, paged.getPageSize());
        assertEquals(List.of(1, 2, 3, 4, 5), paged.getPaginates());
        assertEquals(20, paged.getContents().size());
        assertEquals(41, paged.getContents().get(0).getId());
        assertEquals(60, paged.getContents().get(19).getId());
    }

    @Test
    @Order(4)
    @DisplayName("총 95 아이템,  5페이지, 페이지사이즈 5, 페이지토탈사이즈 20")
    public void page5Size20Total20() {
        PageRequest pageRequest = PageRequest.of(5, 20, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        assertEquals(5, paged.getPageNumber());
        assertEquals(20, paged.getPageSize());
        assertEquals(List.of(1, 2, 3, 4, 5), paged.getPaginates());
        assertEquals(15, paged.getContents().size());
        assertEquals(81, paged.getContents().get(0).getId());
        assertEquals(95, paged.getContents().get(14).getId());
    }

    @Test
    @Order(5)
    @DisplayName("총 95 아이템,  5페이지, 페이지사이즈 5, 페이지토탈사이즈 3")
    public void page4Size20Total5() {
        PageRequest pageRequest = PageRequest.of(4, 20, 3);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        assertEquals(4, paged.getPageNumber());
        assertEquals(20, paged.getPageSize());
        assertEquals(List.of(4,5), paged.getPaginates());
        assertEquals(4, paged.getFirstPagedNumber());
        assertEquals(5, paged.getLastPagedNumber());
        assertEquals(20, paged.getContents().size());
        assertEquals(61, paged.getContents().get(0).getId());
        assertEquals(80, paged.getContents().get(19).getId());
    }

    @Test
    @Order(6)
    @DisplayName("총 95 아이템, 10페이지, 페이지사이즈 1, 페이지토탈사이즈 20")
    public void page10Size1Total20() {
        PageRequest pageRequest = PageRequest.of(10, 1, 20);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        assertEquals(10, paged.getPageNumber());
        assertEquals(1, paged.getPageSize());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), paged.getPaginates());
        assertEquals(1, paged.getFirstPagedNumber());
        assertEquals(20, paged.getLastPagedNumber());
        assertEquals(1, paged.getContents().size());
        assertEquals(10, paged.getContents().get(0).getId());
        assertEquals(10, paged.getContents().get(0).getId());
        assertTrue(paged.hasNext());
        assertTrue(paged.hasPrevious());
    }

    @Test
    @Order(7)
    @DisplayName("총 95 아이템, 10페이지, 페이지사이즈 10, 페이지토탈사이즈 10")
    public void page10Size10Total10() {
        PageRequest pageRequest = PageRequest.of(10, 10, 10);
        List<Post> contents = posts.stream().skip(pageRequest.getOffset()).limit(pageRequest.getRowNumber()).collect(Collectors.toList());
        Paged<Post> paged = new Paged(pageRequest, contents, posts.size());
        assertEquals(10, paged.getPageNumber());
        assertEquals(10, paged.getPageSize());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), paged.getPaginates());
        assertEquals(1, paged.getFirstPagedNumber());
        assertEquals(10, paged.getLastPagedNumber());
        assertEquals(5, paged.getContents().size());
        assertEquals(91, paged.getContents().get(0).getId());
        assertEquals(95, paged.getContents().get(4).getId());
        assertFalse(paged.hasNext());
        assertTrue(paged.hasPrevious());
    }
}