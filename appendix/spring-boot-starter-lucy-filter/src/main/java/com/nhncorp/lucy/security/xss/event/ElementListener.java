
package com.nhncorp.lucy.security.xss.event;

import java.util.EventListener;

import com.nhncorp.lucy.security.xss.markup.Element;

public interface ElementListener extends EventListener {
	
	public void handleElement(Element element);
}
