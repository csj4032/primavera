
package com.nhncorp.lucy.security.xss.markup.rule;

abstract class Terminal extends ParsingRule {
	public abstract boolean sliceToken(Token parent, CharArraySegment input);

	public abstract int matchPos(CharArraySegment input);
}
