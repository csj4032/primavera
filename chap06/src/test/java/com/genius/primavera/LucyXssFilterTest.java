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
@DisplayName("Lucy XSS translated_text_2 translated_text_2 test")
public class LucyXssFilterTest {

    @Test
    @Order(1)
    @DisplayName(value = "translated_text_2 translated_text_2 XssPreventer translated_text_2")
    public void testXssPreventer() {
        String dirty = "\"><script>alert('xss');</script>";
        String clean = XssPreventer.escape(dirty);
        assertEquals("&quot;&gt;&lt;script&gt;alert(&#39;xss&#39;);&lt;/script&gt;", clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(2)
    @DisplayName(value = "translated_text_2 translated_text_2 XssPreventer translated_text_2 - translated_text_1 translated_text_3")
    public void testXssPreventerEmpty() {
        String dirty = "";
        String clean = XssPreventer.escape(dirty);
        assertEquals("", clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(3)
    @DisplayName(value = "translated_text_2 translated_text_2 XssPreventer translated_text_2 - null")
    public void testXssPreventerNull() {
        String dirty = null;
        String clean = XssPreventer.escape(dirty);
        assertNull(clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(4)
    @DisplayName(value = "translated_text_2 translated_text_2 XssPreventer translated_text_2 - translated_text_2")
    public void testXssPreventerWhitespace() {
        String dirty = "   ";
        String clean = XssPreventer.escape(dirty);
        assertEquals("   ", clean);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

    @Test
    @Order(5)
    @DisplayName(value = "translated_text_2 translated_text_2 XssPreventer translated_text_2 - translated_text_4")
    public void testXssPreventerSpecialChars() {
        String dirty = "!@#$%^&*()_+|{}[]:;\"'<>,.?/~`";
        String clean = XssPreventer.escape(dirty);
        assertEquals(dirty, XssPreventer.unescape(clean));
    }

}