
package com.nhncorp.lucy.security.xss;

public class CommonUtils {
	
	public static String getQuotePair(String text) {
		String quotePairStr = text;

		if ( "\"".equals(text)) {
			quotePairStr = "\"\"";
		} else if ( "'".equals(text)) {
			quotePairStr = "''";
		} else if ( text.startsWith("\"") && !text.endsWith("\"")) {
			quotePairStr = quotePairStr + "\"";
		} else if ( text.startsWith("'") && !text.endsWith("'")) {
			quotePairStr = quotePairStr + "'";
		} else if ( !text.startsWith("\"") && text.endsWith("\"")) {
			quotePairStr = "\"" + quotePairStr;
		} else if ( !text.startsWith("'") && text.endsWith("'")) {
			quotePairStr = "'" + quotePairStr;
		}

		return quotePairStr;
	}
}
