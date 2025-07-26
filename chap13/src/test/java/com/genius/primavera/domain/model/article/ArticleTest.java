package com.genius.primavera.domain.model.article;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArticleTest {

    private static Article root;
    private static Article first;
    private static Article first_first;
    private static Article first_second;
    private static Article first_second_first;
    private static Article second;

    @BeforeAll
    public static void setUp() {
        root = Article.builder().id(1).subject("Root").build();
        first = Article.builder().id(1).parent(root).subject("Root 직계 첫번째").build();
        first_first = Article.builder().id(2).parent(first).subject("Root 직계 첫번째 first 직계 첫번째").build();
        first_second = Article.builder().id(4).parent(first).subject("Root 직계 첫번째 first 직계 두번째").build();
        first_second_first = Article.builder().id(5).parent(first_second).subject("Root 직계 첫번째 first 직계 두번째 first_second 첫번째").build();
        second = Article.builder().id(3).parent(root).subject("Root 직계 두번째").build();

        first_second.setChildren(new Article[]{first_second_first});
        first.setChildren(new Article[]{first_first});
        root.setChildren(new Article[]{first, second});

    }

    @Test
    @DisplayName("부모 아티클 확인")
    public void hasParentsTest() {
        Assertions.assertFalse(root.hasParents());
        Assertions.assertEquals(0, root.getLevel());
        Assertions.assertArrayEquals(new Article[]{first, second}, root.getChildren());

        Assertions.assertTrue(first.hasParents());
        Assertions.assertTrue(first.hasChildren());
        Assertions.assertEquals(1, first.getLevel());
        Assertions.assertArrayEquals(new Article[]{second}, first.getSibling());

        Assertions.assertTrue(first_second.hasParents());
        Assertions.assertTrue(first_second.hasChildren());
        Assertions.assertEquals(root, first_second.rootParent());
        Assertions.assertEquals(2, first_second.getLevel());

        Assertions.assertEquals(3, first_second_first.getLevel());

        Assertions.assertEquals(1, second.getLevel());
    }
}