
package com.nhncorp.lucy.security.xss.listener;

import com.nhncorp.lucy.security.xss.event.ElementListener;
import com.nhncorp.lucy.security.xss.markup.Element;

public class EmbedSecurityListener implements ElementListener {
	ContentTypeCacheRepo contentTypeCacheRepo = new ContentTypeCacheRepo();

	public void handleElement(Element element) {
		if (element.isDisabled()) {
			return;
		}

		String srcUrl = element.getAttributeValue("src");
		boolean isWhiteUrl = this.isWhiteUrl(srcUrl);
		boolean isVulnerable = SecurityUtils.checkVulnerableWithHttp(element, srcUrl, isWhiteUrl, contentTypeCacheRepo);

		if (isVulnerable) {
			element.setEnabled(false);
			return;
		}

		element.putAttribute("invokeURLs", "\"false\"");
		element.putAttribute("autostart", "\"false\"");
		element.putAttribute("allowScriptAccess", "\"never\"");

		if (isWhiteUrl) {
			element.putAttribute("allowNetworking", "\"all\"");
		} else {
			element.putAttribute("allowNetworking", "\"internal\"");
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
