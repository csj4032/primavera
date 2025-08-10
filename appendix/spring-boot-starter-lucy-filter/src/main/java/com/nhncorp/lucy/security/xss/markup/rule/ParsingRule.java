	
package com.nhncorp.lucy.security.xss.markup.rule;

abstract class ParsingRule {
	public enum UNARY {
		OPTION, REPEAT0, REPEAT1, ONE;

		public static UNARY getValue(char ch) {
			switch (ch) {
				case '?':
					return OPTION;
				case '*':
					return REPEAT0;
				case '+':
					return REPEAT1;
			}
			return ONE;
		}
	}

	protected UNARY unary = UNARY.ONE;

	public UNARY getUnary() {
		return this.unary;
	}

	public void setUnary(UNARY unary) {
		this.unary = unary;
	}

	public boolean isOptional() {
		if (unary == UNARY.OPTION || unary == UNARY.REPEAT0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isRepeat() {
		if (this.getUnary() == UNARY.REPEAT0 || this.getUnary() == UNARY.REPEAT1) {
			return true;
		} else {
			return false;
		}
	}
}
