
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public abstract class Content {
	
	protected Element parent;

	public Element getParent() {
		return this.parent;
	}

	public void setParent(Element parent) {
		this.parent = parent;
	}

	public abstract void serialize(Writer writer) throws IOException;

	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			this.serialize(writer);
		} catch (IOException ioe) {
		}

		return writer.toString();
	}
}
