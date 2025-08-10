
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.nhncorp.lucy.security.xss.CommonUtils;
import com.nhncorp.lucy.security.xss.markup.rule.CharArraySegment;
import com.nhncorp.lucy.security.xss.markup.rule.ParsingGrammar;
import com.nhncorp.lucy.security.xss.markup.rule.Token;

public final class MarkupParser {
	private static ParsingGrammar grammar = ParsingGrammar.getInstance();

	private MarkupParser() {
	}

	public static Collection<Content> parse(String input) {

		if (input == null || input.length() == 0) {
			return null;
		}

		LinkedList<Content> result = new LinkedList<Content>();

		LinkedList<Element> stack = null;

		CharArraySegment charArraySegment = new CharArraySegment(input);
		Token token;
		while ((token = grammar.nextToken(charArraySegment)) != null) {
			String tokenName = token.getName();
			if ("description".equals(tokenName)) {

				String description = token.getText();
				result.add(new Description(description));

			} else if ("comment".equals(tokenName)) {
				String comment = token.getText();
				if (comment != null && comment.length() != 0) {
					comment = comment.substring(4, comment.length() - 3);
				}
				result.add(new Comment(comment));

			} else if ("iEHExStartTag".endsWith(tokenName)) {

				Element element = new IEHackExtensionElement(token.getText());

				if (stack == null) {
					stack = new LinkedList<Element>();
				}

				stack.addFirst(element);
				result.add(element);

			} else if ("startTag".equals(tokenName)) {
				Token tagNameToken = token.getChild("tagName");
				if (tagNameToken == null) {
					continue;
				}

				Element element = new Element(tagNameToken.getText());
				List<Token> attTokens = token.getChildren("attribute");
				if (attTokens != null) {
					for (Token attToken : attTokens) {
						Token attName = attToken.getChild("attName");
						Token attValue = attToken.getChild("attValue");
						if (attName != null && attValue == null) {
							element.putAttribute(new Attribute(attName.getText()));
						} else if (attName != null && attValue != null) {
							String text = attValue.getText();
							text = CommonUtils.getQuotePair(text);
							element.putAttribute(new Attribute(attName.getText(), text));
						}
					}
				}

				Token closeStartEnd = token.getChild("closeStartEnd");

				if (closeStartEnd == null) {

					if (stack == null) {
						stack = new LinkedList<Element>();
					}

					stack.addFirst(element);

				} else {
					element.setStartClose(true);

				}

				result.add(element);

			} else if ("iEHExEndTag".endsWith(tokenName)) {

				boolean flag = false;
				if (stack != null) {
					LinkedList<Element> tmp = new LinkedList<Element>();
					Element element;
					while (!stack.isEmpty() && (element = stack.removeFirst()) != null) {
						if (element instanceof IEHackExtensionElement) {
							Content content;
							while (!result.isEmpty() && (content = result.getLast()) != null) {
								if (content instanceof Element && content == element) {
									element.setClose(true);
									tmp.clear();
									break;
								} else {
									if (stack.contains(content)) {
										stack.remove(content);
									}

									element.addContent(0, result.removeLast());
								}
							}
							flag = true;
							break;
						} else {
							tmp.add(element);
						}
					}

					if (tmp != null && !tmp.isEmpty()) {
						stack = tmp;
					}
				}

				if (!flag) {
					result.add(new Text(token.getText()));
				}

			} else if ("endTag".equals(tokenName)) {
				Token tagNameToken = token.getChild("tagName");
				boolean flag = false;
				if (tagNameToken == null) {
					continue;
				}

				String tagName = tagNameToken.getText();

				if (stack != null) {
					LinkedList<Element> tmp = new LinkedList<Element>();
					Element element;
					while (!stack.isEmpty() && (element = stack.removeFirst()) != null) {
						if (tagName.equalsIgnoreCase(element.getName())) {
							Content content;
							while (!result.isEmpty() && (content = result.getLast()) != null) {
								if (content instanceof Element && content == element) {
									element.setClose(true);
									tmp.clear();
									break;
								} else {
									if (stack.contains(content)) {
										stack.remove(content);
									}

									element.addContent(0, result.removeLast());
								}
							}
							flag = true;
							break;
						} else {
							tmp.add(element);
						}
					}

					if (tmp != null && !tmp.isEmpty()) {
						stack = tmp;
					}
				}

				if (!flag) {
					result.add(new Text(token.getText()));
				}
			} else {
				result.add(new Text(token.getText()));
			}
		}

		return result;
	}

	public static Collection<Content> parse(InputStream stream, Charset cs) throws IOException {
		return parse(read(new InputStreamReader(stream, cs)));
	}

	private static String read(Reader reader) throws IOException {
		StringBuilder buffer = new StringBuilder();
		try {
			char[] cbuf = new char[1024];
			int rc;
			while ((rc = reader.read(cbuf)) > 0) {
				buffer.append(cbuf, 0, rc);
			}
		} finally {
			reader.close();
		}

		return buffer.toString();
	}

	public static String toString(Collection<Content> contents) {
		if (contents == null) {
			return "";
		}

		StringWriter writer = new StringWriter();
		for (Content content : contents) {
			try {
				content.serialize(writer);
			} catch (IOException e) {
			}
		}

		return writer.toString();
	}
}
