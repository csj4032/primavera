
package com.nhncorp.lucy.security.xss.listener;

import com.nhncorp.lucy.security.xss.event.AttributeListener;
import com.nhncorp.lucy.security.xss.markup.Attribute;

public class SrcAttributeListener implements AttributeListener {
	public void handleAttribute(Attribute attr) {
		if (this.isWhiteUrl(attr.getValue())) {
		} else {
			attr.setValue("\"\"");
		}
	}

	private boolean isWhiteUrl(String url) {
		WhiteUrlList list = WhiteUrlList.getInstance();
		if (list != null && list.contains(url)) {
			return true;
		}
		return false;
	}
}
