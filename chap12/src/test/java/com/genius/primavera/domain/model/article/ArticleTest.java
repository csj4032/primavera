package com.genius.primavera.domain.model.article;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Article 도메인 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleTest {

    private static Article root;
    private static Article first;
    private static Article first_first;
    private static Article first_second;
    private static Article first_second_first;
    private static Article second;

    @BeforeAll
    public static void setUp() {
        root = Article.builder().id(0).pId(0).subject("Root").level(0).build();

        first = Article.builder().id(1).pId(0).parent(root).subject("Root 직계 첫번째").level(1).build();
        first_first = Article.builder().id(2).pId(1).parent(first).subject("Root 직계 첫번째 first 직계 첫번째").level(2).build();
        first_second = Article.builder().id(4).pId(1).parent(first).subject("Root 직계 첫번째 first 직계 두번째").level(2).build();
        first_second_first = Article.builder().id(5).pId(4).parent(first_second).subject("Root 직계 첫번째 first 직계 두번째 first_second 첫번째").level(3).build();

        second = Article.builder().id(3).pId(0).parent(root).subject("Root 직계 두번째").level(1).build();
        first_second.setChildren(new Article[]{first_second_first});
        first.setChildren(new Article[]{first_first, first_second});
        root.setChildren(new Article[]{first, second});
    }

    @Test
    @Order(1)
    @DisplayName("부모 아티클 확인")
    public void hasParentsTest() {
        assertFalse(root.hasParents());
        assertEquals(0, root.getLevel());
        assertArrayEquals(new Article[]{first, second}, root.getChildren());

        assertTrue(first.hasParents());
        assertTrue(first.hasChildren());
        assertEquals(1, first.getLevel());
        assertArrayEquals(new Article[]{first, second}, first.getSibling());

        assertTrue(first_second.hasParents());
        assertTrue(first_second.hasChildren());
        assertEquals(root, first_second.rootParent());
        assertEquals(2, first_second.getLevel());
        assertEquals(3, first_second_first.getLevel());
        assertEquals(1, second.getLevel());
    }
}