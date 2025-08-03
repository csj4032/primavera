package com.genius.primavera;

import com.nhncorp.lucy.security.xss.XssPreventer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Lucy XSS 필터 단위 테스트")
public class LucyXssFilterTest {

    @Test
    @Order(1)
    @DisplayName(value = "루시 필터 XssPreventer 검사")
    public void testXssPreventer() {
        String dirty = "\"><script>alert('xss');</script>";
        String clean = XssPreventer.escape(dirty);
        assertEquals("&quot;&gt;&lt;script&gt;alert(&#39;xss&#39;);&lt;/script&gt;", clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(2)
    @DisplayName(value = "루시 필터 XssPreventer 검사 - 빈 문자열")
    public void testXssPreventerEmpty() {
        String dirty = "";
        String clean = XssPreventer.escape(dirty);
        assertEquals("", clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(3)
    @DisplayName(value = "루시 필터 XssPreventer 검사 - null")
    public void testXssPreventerNull() {
        String dirty = null;
        String clean = XssPreventer.escape(dirty);
        assertNull(clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(4)
    @DisplayName(value = "루시 필터 XssPreventer 검사 - 공백")
    public void testXssPreventerWhitespace() {
        String dirty = "   ";
        String clean = XssPreventer.escape(dirty);
        assertEquals("   ", clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(5)
    @DisplayName(value = "루시 필터 XssPreventer 검사 - 특수문자")
    public void testXssPreventerSpecialChars() {
        String dirty = "!@#$%^&*()_+|{}[]:;\"'<>,.?/~`";
        String clean = XssPreventer.escape(dirty);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

}