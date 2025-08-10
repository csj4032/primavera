
package com.nhncorp.lucy.security.xss.event;

import java.util.EventListener;

import com.nhncorp.lucy.security.xss.markup.Attribute;

public interface AttributeListener extends EventListener {
	
	public void handleAttribute(Attribute attr);
}
