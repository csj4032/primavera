
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

public class IEHackExtensionElement extends Element {
	public IEHackExtensionElement(String name) {
		super(name);
	}

	@Override
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public void serialize(Writer writer) throws IOException {

		String stdName = getName().replaceAll("-->", ">").replaceFirst("<!--\\s*", "<!--").replaceAll("]\\s*>", "]>");

		int startIndex = stdName.indexOf("<!") + 1;
		int lastIntndex = stdName.lastIndexOf(">");

		String firststdName = stdName.substring(0, startIndex);
		String middlestdName = StringUtils.replaceEach(stdName.substring(startIndex, lastIntndex), new String[] {"<", ">"}, new String[] {"&lt;", "&gt;"});
		String laststdName = stdName.substring(lastIntndex);

		stdName = firststdName + middlestdName + laststdName;

		writer.write(stdName);
	}
}
