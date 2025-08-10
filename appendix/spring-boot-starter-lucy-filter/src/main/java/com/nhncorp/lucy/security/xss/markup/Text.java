	
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.Writer;

public class Text extends Content {
	
	protected String text;

	public Text(String text) {
		this.text = (text == null) ? "" : text;
	}

	public void serialize(Writer writer) throws IOException {
		if (writer == null) {
			return;
		}

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
	}
}
