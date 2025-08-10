
package com.nhncorp.lucy.security.xss.markup;

import com.nhncorp.lucy.security.xss.markup.rule.CharArraySegment;
import com.nhncorp.lucy.security.xss.markup.rule.ParsingGrammar;
import com.nhncorp.lucy.security.xss.markup.rule.Token;

public final class MarkupSaxParser {
	private static ParsingGrammar grammar = ParsingGrammar.getInstance();

	private MarkupSaxParser() {
	}

	public static Token parse(CharArraySegment charArraySegment) {
		Token token = grammar.nextToken(charArraySegment);
		return token;
	}
}
