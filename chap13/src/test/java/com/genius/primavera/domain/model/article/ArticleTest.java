package com.genius.primavera.domain.model.article;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers(containers = {ContainerType.MARIADB, ContainerType.MONGODB})
public class ArticleTest {

    private static Article root;
    private static Article first;
    private static Article first_first;
    private static Article first_second;
    private static Article first_second_first;
    private static Article second;

    @BeforeAll
    public static void setUp() {
        root = Article.builder().id(1).level(0).subject("Root").build();
        first = Article.builder().id(1).parent(root).level(1).subject("Root 직계 첫번째").build();
        first_first = Article.builder().id(2).parent(first).level(2).subject("Root 직계 첫번째 first 직계 첫번째").build();
        first_second = Article.builder().id(4).parent(first).level(2).subject("Root 직계 첫번째 first 직계 두번째").build();
        first_second_first = Article.builder().id(5).parent(first_second).level(3).subject("Root 직계 첫번째 first 직계 두번째 first_second 첫번째").build();
        second = Article.builder().id(3).parent(root).level(1).subject("Root 직계 두번째").build();

        first_second.setChildren(new Article[]{first_second_first});
        first.setChildren(new Article[]{first_first});
        root.setChildren(new Article[]{first, second});

    }

    @Test
    @DisplayName("부모 아티클 확인")
    public void hasParentsTest() {
        assertFalse(root.hasParents());
        assertEquals(0, root.getLevel());
        assertArrayEquals(new Article[]{first, second}, root.getChildren());
        assertTrue(first.hasParents());
        assertTrue(first.hasChildren());
        assertEquals(1, first.getLevel());
        Article[] siblings = first.getSibling();
        assertEquals(2, siblings.length);
        assertTrue(java.util.Arrays.asList(siblings).contains(first));
        assertTrue(java.util.Arrays.asList(siblings).contains(second));
        assertTrue(first_second.hasParents());
        assertTrue(first_second.hasChildren());
        assertEquals(root, first_second.rootParent());
        assertEquals(2, first_second.getLevel());
        assertEquals(3, first_second_first.getLevel());
        assertEquals(1, second.getLevel());
    }
}