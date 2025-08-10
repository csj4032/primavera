	
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.Writer;

public class Description extends Content {
	
	protected String text;

	public Description(String text) {
		this.text = (text == null) ? "" : text;
	}

	@Override
	public void serialize(Writer writer) throws IOException {
		if (writer == null) {
			return;
		}

		writer.write(this.text);
	}

}
