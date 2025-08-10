
package com.nhncorp.lucy.security.xss.markup.rule;

class Literal extends Terminal {
	private String literal;

	public Literal(String literal) {
		this.literal = (literal == null) ? "" : literal;
	}

	public String getLiteral() {
		return this.literal;
	}

	public boolean sliceToken(Token parent, CharArraySegment input) {
		boolean isTokenized = false;
		do {
			if (input != null && input.hasRemaining() && input.startWith(this.literal)) {
				parent.appendValue(input.slice(this.literal.length()));
				isTokenized = true;
			} else {
				break;
			}
		} while (this.isRepeat());

		return isTokenized;
	}

	public int matchPos(CharArraySegment input) {
		return input.posOf(this.literal);
	}
}
