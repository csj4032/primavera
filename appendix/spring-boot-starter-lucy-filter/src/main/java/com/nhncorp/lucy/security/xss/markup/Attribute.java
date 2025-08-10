
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Attribute {
	
	protected String name;
	
	protected String value;
	
	protected boolean enabled = true;

	public Attribute(String name) {
		this.name = name;
	}

	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return (this.name == null) ? "" : this.name;
	}

	public String getValue() {
		return (this.value == null) ? "" : this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isMinimized() {
		return (this.value == null) ? true : false;
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			this.serialize(writer);
		} catch (IOException ioe) {
		}

		return writer.toString();
	}

	public void serialize(Writer writer) throws IOException {
		if (writer == null) {
			return;
		}

		writer.write(this.getName());
		if (!this.isMinimized()) {
			writer.write('=');
			String value = this.getValue();
			int pos = 0;
			int length = value.length();

			for (int i = 0; i < length; i++) {
				if (value.charAt(i) == '<') {
					if (i > pos) {
						writer.write(value, pos, i - pos);
					}
					writer.write("&lt;");
					pos = i + 1;
				} else if (value.charAt(i) == '>') {
					if (i > pos) {
						writer.write(value, pos, i - pos);
					}
					writer.write("&gt;");
					pos = i + 1;
				}
			}

			if (length > pos) {
				writer.write(value, pos, length - pos);
			}
		}
	}

	public boolean isDisabled() {
		return !this.enabled;
	}

	public void setEnabled(boolean flag) {
		this.enabled = flag;
	}
}
