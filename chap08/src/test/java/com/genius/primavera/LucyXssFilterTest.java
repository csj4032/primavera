package com.genius.primavera;

import com.nhncorp.lucy.security.xss.XssPreventer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
}
