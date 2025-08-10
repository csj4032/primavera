
package com.nhncorp.lucy.security.xss.markup.rule;

import java.util.List;

abstract class NonTerminal extends ParsingRule {
	public abstract String getRuleName();

	public abstract boolean sliceTokens(Token parent, CharArraySegment input, ParsingGrammar grammar);

	public abstract int matchPos(CharArraySegment input, ParsingGrammar grammar);

	public abstract List<Terminal> getFirstNonOptTerminals(ParsingGrammar grammar);

	public abstract Token nextToken(Token token, CharArraySegment input, ParsingGrammar instance);
}
