
package com.nhncorp.lucy.security.xss.markup.rule;

import java.util.ArrayList;
import java.util.List;

public final class Token {
	private String name;
	private CharArraySegment value;
	private ArrayList<Token> children;

	Token(String name) {
		this.name = name;
	}

	public String getName() {
		return (this.name == null) ? "" : this.name;
	}

	void setValue(CharArraySegment value) {
		this.value = value;
		if (value == null && this.children != null) {
			this.children.clear();
			this.children = null;
		}
	}

	void appendValue(CharArraySegment value) {
		if (this.value == null) {
			this.value = new CharArraySegment(value.getArray(), value.index(0), value.length());
		} else {
			this.value.concate(value);
		}
	}

	CharArraySegment getValue() {
		return this.value;
	}

	public String getText() {
		if (this.value == null) {
			return "";
		}
		return this.value.toString();
	}

	void addChild(Token child) {
		if (child == null || child.value == null) {
			return;
		}

		this.appendValue(child.value);
		if (this.children == null) {
			this.children = new ArrayList<Token>();
		}

		if (!this.getName().equals(child.getName())) {
			this.children.add(child);
		} else if (child.getChildCount() > 0) {
			this.children.addAll(child.getChildren());
		}
	}

	void addChildren(List<Token> children) {
		if (children == null || children.isEmpty()) {
			return;
		}

		for (Token node : children) {
			this.addChild(node);
		}
	}

	public Token getChild(int index) {
		return (this.children == null) ? null : this.children.get(index);
	}

	public Token getChild(String name) {
		if (this.children == null || this.children.isEmpty()) {
			return null;
		}

		Token child = null;
		for (Token token : this.children) {
			if (token.getName().equals(name)) {
				child = token;
				break;
			}
		}

		return child;
	}

	public List<Token> getChildren() {
		return this.children;
	}

	public List<Token> getChildren(String name) {
		if (this.children == null || this.children.isEmpty()) {
			return null;
		}

		ArrayList<Token> list = null;
		for (Token token : this.children) {
			if (token.getName().equals(name)) {
				list = (list == null) ? new ArrayList<Token>() : list;
				list.add(token);
			}
		}

		return list;
	}

	public int getChildCount() {
		return (this.children == null) ? 0 : this.children.size();
	}

	@Override
	public String toString() {
		return this.toString(0);
	}

	private String toString(int level) {
		StringBuilder indentSb = new StringBuilder();

		for (int i = 0; i < level; i++) {
			indentSb.append("\t");
		}

		StringBuilder buffer = new StringBuilder(String.format("%s%s : [%s]", indentSb.toString(), this.getName(), this.getText()));
		if (this.getChildCount() > 0) {
			for (Token child : this.getChildren()) {
				buffer.append("\n");
				buffer.append(child.toString(level + 1));
			}
		}

		return buffer.toString();
	}
}
