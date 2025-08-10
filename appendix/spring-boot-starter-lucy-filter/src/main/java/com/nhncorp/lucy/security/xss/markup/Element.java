
package com.nhncorp.lucy.security.xss.markup;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Element extends Content {
	
	protected String name;
	
	protected Map<String, Attribute> atts;
	
	protected List<Content> contents;
	
	protected boolean isClosed;

	protected boolean isStartClosed;

	protected boolean enabled = true;

	protected boolean removed = false;

	public Element(String name) {
		this.name = name;
	}

	public String getName() {
		return (this.name == null) ? "" : this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isClosed() {
		return this.isClosed;
	}

	public void setClose(boolean close) {
		this.isClosed = close;
	}

	public boolean isStartClosed() {
		return this.isStartClosed;
	}

	public void setStartClose(boolean startClose) {
		this.isStartClosed = startClose;
	}

	public void putAttribute(String attName, String attValue) {
		if (this.atts == null) {
			this.atts = new LinkedHashMap<String, Attribute>();
		}

		this.atts.put(attName.toLowerCase(), new Attribute(attName, attValue));
	}

	public void putAttribute(Attribute att) {
		if (att == null) {
			return;
		}

		if (this.atts == null) {
			this.atts = new LinkedHashMap<String, Attribute>();
		}

		this.atts.put(att.getName().toLowerCase(), att);
	}

	public Attribute getAttribute(String attName) {
		if (this.atts == null || attName == null) {
			return null;
		}

		return this.atts.get(attName.toLowerCase());
	}

	public String getAttributeValue(String attName) {
		if (this.atts == null || attName == null) {
			return "";
		}

		String value = "";
		Attribute att = this.atts.get(attName.toLowerCase());
		if (att != null) {
			value = att.getValue();
		}
		return value;
	}

	public Collection<Attribute> getAttributes() {
		if (this.atts == null) {
			return null;
		}

		return this.atts.values();
	}

	public boolean isEmpty() {
		if (this.contents != null && !this.contents.isEmpty()) {
			return false;
		}

		return true;
	}

	public List<Content> getContents() {
		return (this.isEmpty()) ? null : this.contents;
	}

	public List<Element> getElements() {
		if (this.isEmpty()) {
			return null;
		}

		List<Element> elements = new ArrayList<Element>();
		for (Content content : this.contents) {
			if (content instanceof Element) {
				elements.add(Element.class.cast(content));
			}
		}

		return elements;
	}

	public int indexOf(Content content) {
		if (this.isEmpty()) {
			return -1;
		}

		return this.contents.indexOf(content);
	}

	public void addContent(Content content) {
		if (content == null) {
			return;
		}

		content.setParent(this);
		if (this.contents == null) {
			this.contents = new ArrayList<Content>();
		}

		this.contents.add(content);
	}

	public void addContent(int index, Content content) {
		if (content == null) {
			return;
		}

		content.setParent(this);
		if (this.contents == null) {
			this.contents = new ArrayList<Content>();
		}

		this.contents.add(index, content);
	}

	public void setContent(int index, Content content) {
		if (content == null) {
			return;
		}

		content.setParent(this);
		if (this.contents == null) {
			this.contents = new ArrayList<Content>();
		}

		this.contents.set(index, content);
	}

	public void addContents(Collection<? extends Content> contents) {
		if (contents == null) {
			return;
		}

		for (Content content : contents) {
			this.addContent(content);
		}
	}

	public void removeContent(Content content) {
		if (content != null && !this.isEmpty()) {
			this.contents.remove(content);
		}
	}

	public void removeContent(int index) {
		if (!this.isEmpty()) {
			this.contents.remove(index);
		}
	}

	public Element getElementById(String id) {
		if (id == null || this.isEmpty()) {
			return null;
		}

		Element result = null;
		for (Content content : this.contents) {
			if (content instanceof Element) {
				Element element = Element.class.cast(content);
				if (id.equals(element.getAttributeValue("id"))) {
					result = element;
				} else {
					result = element.getElementById(id);
				}

				if (result != null) {
					break;
				}
			}
		}

		return result;
	}

	public List<Element> getElementsByTagName(String tagName) {
		if (tagName == null || this.isEmpty()) {
			return null;
		}

		List<Element> result = new ArrayList<Element>();
		for (Content content : this.contents) {
			if (content instanceof Element) {
				Element element = Element.class.cast(content);
				if (element.getName().equalsIgnoreCase(tagName)) {
					result.add(element);
				}

				if (!element.isEmpty()) {
					result.addAll(element.getElementsByTagName(tagName));
				}
			}
		}

		return result;
	}

	public void serialize(Writer writer) throws IOException {
		if (writer == null) {
			return;
		}

		writer.write('<');
		writer.write(this.getName());

		if (this.atts != null && !this.atts.isEmpty()) {
			for (Attribute att : this.atts.values()) {
				writer.write(' ');
				att.serialize(writer);
			}
		}
		writer.write('>');

		if (!this.isEmpty()) {
			for (Content content : this.contents) {
				content.serialize(writer);
			}
		}

		if (this.isClosed) {
			writer.write("</");
			writer.write(this.getName());
			writer.write('>');
		}
	}

	public boolean isDisabled() {
		return !this.enabled;
	}

	public void setEnabled(boolean flag) {
		this.enabled = flag;
	}

	public boolean existDisabledAttribute() {
		boolean flag = false;
		Collection<Attribute> atts = this.getAttributes();
		if (atts != null && !atts.isEmpty()) {
			for (Attribute att : atts) {
				if (att.isDisabled()) {
					flag = true;
					break;
				}
			}
		}

		return flag;
	}

	public boolean removeAllAttributes() {

		if (this.atts != null && !this.atts.isEmpty()) {

			this.atts.clear();

			return true;
		}

		return false;
	}

	public Attribute removeAttribute(String attriName) {
		if(this.atts == null) {
			return null;
		}

		return this.atts.remove(attriName);
	}

	public boolean removeAllContents() {

		if (this.contents != null && !this.contents.isEmpty()) {

			this.contents.clear();

			return true;
		}

		return false;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}
}
