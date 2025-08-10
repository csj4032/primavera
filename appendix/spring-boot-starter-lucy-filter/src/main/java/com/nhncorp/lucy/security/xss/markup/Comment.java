	
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.Writer;

import com.nhncorp.lucy.security.xss.LucyXssFilter;

public class Comment extends Content {
	
	protected String text;

	public Comment(String text) {
		this.text = (text == null) ? "" : text;
	}

	public void serialize(Writer writer) throws IOException {
		if (writer == null) {
			return;
		}

		writer.write("<!--");

		int pos = 0;
		int length = this.text.length();

		for (int i = 0; i < length; i++) {
			if (this.text.charAt(i) == '<') {
				if (i > pos) {
					writer.write(this.text, pos, i - pos);
				}
				writer.write("&lt;");
				pos = i + 1;
			} else if (this.text.charAt(i) == '>') {
				if (i > pos) {
					writer.write(this.text, pos, i - pos);
				}
				writer.write("&gt;");
				pos = i + 1;
			}
		}

		if (length > pos) {
			writer.write(this.text, pos, length - pos);
		}

		writer.write("-->");
	}

	public void serializeNoop(Writer writer) throws IOException {
		if (writer == null) {
			return;
		}

		writer.write("<!--");

		writer.write(this.text);

		writer.write("-->");
	}

	public void serializeFilteringTagInComment(Writer writer, LucyXssFilter filter) throws IOException {

		if (writer == null) {
			return;
		}

		if (filter == null) {

			serialize(writer);
			return;
		}

		writer.write("<!--");

		filter.doFilter(text, writer);

		writer.write("-->");
	}

	public void serializeFilteringTagInComment(Writer writer, boolean isFilteringTagInCommentEnabled, LucyXssFilter commentFilter) throws IOException {

		if (isFilteringTagInCommentEnabled) {
			this.serializeFilteringTagInComment(writer, commentFilter);
		} else {
			this.serializeNoop(writer);
		}
	}

}
