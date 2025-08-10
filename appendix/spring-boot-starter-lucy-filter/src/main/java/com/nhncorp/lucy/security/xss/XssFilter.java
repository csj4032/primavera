
package com.nhncorp.lucy.security.xss;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nhncorp.lucy.security.xss.config.AttributeRule;
import com.nhncorp.lucy.security.xss.config.ElementRule;
import com.nhncorp.lucy.security.xss.config.XssConfiguration;
import com.nhncorp.lucy.security.xss.markup.Attribute;
import com.nhncorp.lucy.security.xss.markup.Comment;
import com.nhncorp.lucy.security.xss.markup.Content;
import com.nhncorp.lucy.security.xss.markup.Description;
import com.nhncorp.lucy.security.xss.markup.Element;
import com.nhncorp.lucy.security.xss.markup.IEHackExtensionElement;
import com.nhncorp.lucy.security.xss.markup.MarkupParser;
import com.nhncorp.lucy.security.xss.markup.Text;

public final class XssFilter implements LucyXssFilter {
	private static final Log LOG = LogFactory.getLog(XssFilter.class);

	private static final String BAD_TAG_INFO = "<!-- Not Allowed Tag Filtered -->";
	private static final String BAD_ATT_INFO_START = "<!-- Not Allowed Attribute Filtered (";
	private static final String BAD_ATT_INFO_END = ") -->";
	private static final String REMOVE_TAG_INFO_START = "<!-- Removed Tag Filtered (";
	private static final String REMOVE_TAG_INFO_END = ") -->";
	private static final String CONFIG = "lucy-xss-superset.xml";
	private static final String IE_HACK_EXTENSION = "IEHackExtension";
	private boolean withoutComment;
	private String service;
	private String blockingPrefix;
	private boolean blockingPrefixEnabled;
	private boolean filteringTagInCommentEnabled;

	private XssFilter commentFilter;
	private XssConfiguration config;

	private static final Map<FilterRepositoryKey, XssFilter> instanceMap = new HashMap<FilterRepositoryKey, XssFilter>();

	private XssFilter(XssConfiguration config) {
		this.config = config;
	}

	public static XssFilter getInstance() throws XssFilterException {
		return getInstance(CONFIG, false);
	}

	public static XssFilter getInstance(boolean withoutComment) throws XssFilterException {
		return getInstance(CONFIG, withoutComment);
	}

	public static XssFilter getInstance(String fileName) throws XssFilterException {
		return getInstance(fileName, false);
	}

	public static XssFilter getInstance(String fileName, boolean withoutComment) throws XssFilterException {
		try {
			synchronized (XssFilter.class) {
				FilterRepositoryKey key = new FilterRepositoryKey(fileName, withoutComment);

				XssFilter filter = instanceMap.get(key);
				if (filter != null) {
					filter.withoutComment = withoutComment;
					return filter;
				}
				filter = new XssFilter(XssConfiguration.newInstance(fileName));
				filter.service = filter.config.getService();
				filter.blockingPrefixEnabled = filter.config.isEnableBlockingPrefix();
				filter.blockingPrefix = filter.config.getBlockingPrefix();

				filter.withoutComment = withoutComment;

				filter.filteringTagInCommentEnabled = filter.config.isFilteringTagInCommentEnabled();

				if (filter.filteringTagInCommentEnabled && ! filter.config.isNoTagAllowedInComment()) {

					filter.commentFilter = XssFilter.getCommentFilterInstance(filter.config);

				}

				instanceMap.put(key, filter);

				return filter;
			}
		} catch (Exception e) {
			throw new XssFilterException(e);
		}
	}

	public static XssFilter getCommentFilterInstance(XssConfiguration config) {
		XssFilter filter = new XssFilter(config);
		filter.service = filter.config.getService();
		filter.blockingPrefixEnabled = filter.config.isEnableBlockingPrefix();
		filter.blockingPrefix = filter.config.getBlockingPrefix();
		filter.withoutComment = true;
		filter.filteringTagInCommentEnabled = true;

		return filter;
	}

	public XssConfiguration getConfig() {
		return this.config;
	}

	public String doFilter(String dirty) {
		StringWriter writer = new StringWriter();
		doFilter(dirty, writer);
		return writer.toString();
	}

	public void doFilter(String dirty, Writer writer) {
		StringWriter logWriter = new StringWriter();

		if (StringUtils.isEmpty(dirty)) {
			LOG.debug("Source string is empty. doFilter() method end.");
			return;
		}

		Collection<Content> contents = MarkupParser.parse(dirty);

		if (isEmpty(contents)) {
			return;
		}

		try {
			this.serialize(writer, contents);
		} catch (IOException ioe) {

		}
	}

	public String doFilter(String tagName, String attName, String dirtyAttValue) {
		if (tagName == null || tagName.length() == 0 || attName == null || attName.length() == 0 || dirtyAttValue == null || dirtyAttValue.length() == 0) {
			return "";
		}

		StringBuilder dirty = new StringBuilder();
		dirty.append('<').append(tagName);
		dirty.append(' ').append(attName).append('=').append(dirtyAttValue);
		dirty.append('>').append("</").append(tagName).append('>');

		Collection<Content> contents = MarkupParser.parse(dirty.toString());
		if (isEmpty(contents)) {
			return "";
		}

		for (Content content : contents) {
			if (content instanceof Element) {
				Element tag = Element.class.cast(content);
				this.checkRule(tag);

				Attribute att = tag.getAttribute(attName);
				if (att != null) {
					if (att.isDisabled()) {
						return "";
					} else {
						return att.getValue();
					}
				}
			}
		}

		return "";
	}

	private boolean isEmpty(Collection<Content> contents) {
		return contents == null || contents.isEmpty();
	}

	private void serialize(Writer writer, Collection<Content> contents) throws IOException {
		for (Content content : contents) {
			if (content instanceof Text || content instanceof Description) {
				content.serialize(writer);
			} else if (content instanceof Comment) {
				this.serialize(writer, Comment.class.cast(content));
			} else if (content instanceof IEHackExtensionElement) {
				this.serialize(writer, IEHackExtensionElement.class.cast(content));
			} else if (content instanceof Element) {
				this.serialize(writer, Element.class.cast(content));
			}
		}

	}

	private void serialize(Writer writer, Comment comment) throws IOException {
		comment.serializeFilteringTagInComment(writer, this.filteringTagInCommentEnabled, this.commentFilter);
	}

	private void serialize(Writer writer, IEHackExtensionElement ie) throws IOException {

		ElementRule iEHExRule = this.config.getElementRule(IE_HACK_EXTENSION);

		if (iEHExRule != null) {
			iEHExRule.checkEndTag(ie);
			iEHExRule.checkDisabled(ie);
			iEHExRule.excuteListener(ie);
		} else {
			ie.setEnabled(false);
		}

		if (writer == null) {
			return;
		}

		if (ie.isDisabled()) {
			if (!this.withoutComment) {
				writer.write(REMOVE_TAG_INFO_START);
				writer.write(ie.getName().replaceAll("<", "&lt;").replaceFirst(">", "&gt;"));
				writer.write(REMOVE_TAG_INFO_END);
			}

			if (!ie.isEmpty()) {
				this.serialize(writer, ie.getContents());
			}
		} else {

			ie.serialize(writer);

			if (!ie.isEmpty()) {
				this.serialize(writer, ie.getContents());
			}

			if (ie.isClosed()) {

				String stdName = ie.getName().replaceAll("-->", ">").replaceFirst("<!--\\s*", "<!--").replaceAll("]\\s*>", "]>");

				if(stdName.indexOf("<!--") != -1) {
					writer.write("<![endif]-->");
				} else {
					writer.write("<![endif]>");
				}
			}
		}
	}

	private void serialize(Writer writer, Element element) throws IOException {
		boolean hasAttrXss = false;
		checkRuleRemove(element);

		if (element.isRemoved()) {
			if (!this.withoutComment) {
				writer.write(REMOVE_TAG_INFO_START);
				writer.write(element.getName());
				writer.write(REMOVE_TAG_INFO_END);
			}

			if (!element.isEmpty()) {
				this.serialize(writer, element.getContents());
			}
		} else {

			if ((!element.isDisabled() || this.blockingPrefixEnabled)) {
				checkRule(element);
			}

			if (element.isDisabled()) {
				if (this.blockingPrefixEnabled) {
					element.setName(this.blockingPrefix + element.getName());
					element.setEnabled(true);
				} else {
					if (!this.withoutComment) {
						writer.write(BAD_TAG_INFO);
					}

					writer.write("&lt;");
					writer.write(element.getName());

				}
			}

			if (!element.isDisabled() && !this.withoutComment && element.existDisabledAttribute()) {
				writer.write(BAD_ATT_INFO_START);
			}

			Collection<Attribute> atts = element.getAttributes();

			StringWriter attrSw = new StringWriter();
			StringWriter attrXssSw = new StringWriter();

			if (atts != null && !atts.isEmpty()) {
				for (Attribute att : atts) {
					if (!element.isDisabled() && att.isDisabled()) {
						hasAttrXss = true;

						if (!this.withoutComment) {
							attrXssSw.write(' ');
							att.serialize(attrXssSw);
						}
					} else {
						attrSw.write(' ');
						att.serialize(attrSw);
					}
				}
			}

			if (hasAttrXss) {
				String attrXssString = attrXssSw.toString();
				if (!this.withoutComment) {
					writer.write(attrXssString);
					writer.write(BAD_ATT_INFO_END);
				}
			}

			if (!element.isDisabled()) {
				writer.write('<');
				writer.write(element.getName());
			}

			writer.write(attrSw.toString());

			if (element.isStartClosed()) {

				writer.write(element.isDisabled() ? " /&gt;" : " />");

			} else {

				writer.write(element.isDisabled() ? "&gt;" : ">");
			}

			if (!element.isEmpty()) {
				this.serialize(writer, element.getContents());
			}

			if (element.isClosed()) {
				if (element.isDisabled() && !this.blockingPrefixEnabled) {
					writer.write("&lt;/");
					writer.write(element.getName());
					writer.write("&gt;");
				} else {
					writer.write("</");
					writer.write(element.getName());
					writer.write('>');
				}
			}
		}
	}

	private void checkRuleRemove(Element element) {
		ElementRule tagRule = this.config.getElementRule(element.getName());
		if (tagRule == null) {
			element.setEnabled(false);
			return;
		}

		tagRule.checkRemoveTag(element);
		if (element.isRemoved()) {
			tagRule.excuteListener(element);
		}
	}

	private void checkRule(Element element) {

		ElementRule tagRule = this.config.getElementRule(element.getName());
		if (tagRule == null) {

			tagRule = new ElementRule(element.getName());
		}

		tagRule.checkEndTag(element);
		tagRule.checkDisabled(element);
		tagRule.disableNotAllowedAttributes(element);
		tagRule.disableNotAllowedChildElements(element);

		Collection<Attribute> atts = element.getAttributes();
		if (atts != null && !atts.isEmpty()) {
			for (Attribute att : atts) {
				if (att.isDisabled() || att.isMinimized()) {
					continue;
				}
				AttributeRule attRule = this.config.getAttributeRule(att.getName());
				if (attRule == null) {
					att.setEnabled(false);
				} else {
					if (!attRule.getExceptionTagList().contains(element.getName().toLowerCase())) {

						attRule.checkDisabled(att);
					} else {

						attRule.checkDisabled(att);
						att.setEnabled(att.isDisabled());
					}
					attRule.checkAttributeValue(att);
					attRule.executeListener(att);
				}
			}
		}

		tagRule.excuteListener(element);
	}
}
