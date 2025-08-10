
package com.nhncorp.lucy.security.xss;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XssPreventer {

	private static final Log LOG = LogFactory.getLog(XssFilter.class);
	private static Pattern escapePattern = Pattern.compile("'");
	private static Pattern unescapePattern = Pattern.compile("&#39;");

	public static String escape(String dirty) {

		String clean = StringEscapeUtils.escapeHtml4(dirty);

		if (clean == null) {
			return null;
		}

		Matcher matcher = escapePattern.matcher(clean);

		if (matcher.find()) {
			return matcher.replaceAll("&#39;");
		}

		return clean;
	}

	public static String unescape(String clean) {

		String str = StringEscapeUtils.unescapeHtml4(clean);

		if (str == null) {
			return null;
		}

		Matcher matcher = unescapePattern.matcher(str);

		if (matcher.find()) {
			return matcher.replaceAll("'");
		}

		return str;
	}
}
