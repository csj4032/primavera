
package com.nhncorp.lucy.security.xss;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nhncorp.lucy.security.xss.config.AttributeRule;
import com.nhncorp.lucy.security.xss.config.ElementRule;
import com.nhncorp.lucy.security.xss.config.XssConfiguration;
import com.nhncorp.lucy.security.xss.config.XssSaxConfiguration;
import com.nhncorp.lucy.security.xss.listener.SecurityUtils;
import com.nhncorp.lucy.security.xss.listener.WhiteUrlList;
import com.nhncorp.lucy.security.xss.markup.Attribute;
import com.nhncorp.lucy.security.xss.markup.Comment;
import com.nhncorp.lucy.security.xss.markup.Description;
import com.nhncorp.lucy.security.xss.markup.Element;
import com.nhncorp.lucy.security.xss.markup.IEHackExtensionElement;
import com.nhncorp.lucy.security.xss.markup.MarkupSaxParser;
import com.nhncorp.lucy.security.xss.markup.Text;
import com.nhncorp.lucy.security.xss.markup.rule.CharArraySegment;
import com.nhncorp.lucy.security.xss.markup.rule.Token;

public final class XssSaxFilter implements LucyXssFilter {
	private static final Log LOG = LogFactory.getLog(XssSaxFilter.class);

	private static final String BAD_TAG_INFO = "<!-- Not Allowed Tag Filtered -->";
	private static final String BAD_ATT_INFO_START = "<!-- Not Allowed Attribute Filtered (";
	private static final String BAD_ATT_INFO_END = ") -->";
	private static final String REMOVE_TAG_INFO_START = "<!-- Removed Tag Filtered (";
	private static final String REMOVE_TAG_INFO_END = ") -->";
	
	private static final String CONFIG = "lucy-xss-superset-sax.xml";
	private static final String IE_HACK_EXTENSION = "IEHackExtension";
	private boolean withoutComment;

	private String service;
	
	private String blockingPrefix;
	private boolean blockingPrefixEnabled;
	private boolean filteringTagInCommentEnabled;

	private XssSaxFilter commentFilter;
	private XssSaxConfiguration config;

	private static final Map<FilterRepositoryKey, XssSaxFilter> instanceMap = new HashMap<FilterRepositoryKey, XssSaxFilter>();

	private static final Pattern[] PARAMLIST = {Pattern.compile("['\"]?\\s*(?i:invokeURLs)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:autostart)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:allowScriptAccess)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:allowNetworking)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:autoplay)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:enablehref)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:enablejavascript)\\s*['\"]?"),
		Pattern.compile("['\"]?\\s*(?i:nojava)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:AllowHtmlPopupwindow)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:enableHtmlAccess)\\s*['\"]?")};

	private static final Pattern[] URLNAMES = {Pattern.compile("['\"]?\\s*(?i:url)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:href)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:src)\\s*['\"]?"), Pattern.compile("['\"]?\\s*(?i:movie)\\s*['\"]?")};

	private static boolean containsURLName(String name) {
		for (Pattern pattern : URLNAMES) {
			if (pattern.matcher(name).matches()) {
				return true;
			}
		}
		return false;
	}

	private boolean isWhiteUrl(String url) {
		WhiteUrlList list = WhiteUrlList.getInstance();
		if (list != null && list.contains(url)) {
			return true;
		}

		return false;
	}

	private XssSaxFilter(XssSaxConfiguration config) {
		this.config = config;
	}

	public static XssSaxFilter getInstance() throws XssFilterException {
		return getInstance(CONFIG, false);
	}

	public static XssSaxFilter getInstance(boolean withoutComment) throws XssFilterException {
		return getInstance(CONFIG, withoutComment);
	}

	public static XssSaxFilter getInstance(String fileName) throws XssFilterException {
		return getInstance(fileName, false);
	}

	public static XssSaxFilter getInstance(String fileName, boolean withoutComment) throws XssFilterException {
		
		try {
			synchronized (XssSaxFilter.class) {
				FilterRepositoryKey key = new FilterRepositoryKey(fileName, withoutComment);

				XssSaxFilter filter = instanceMap.get(key);
				if (filter != null) {
					return filter;
				}

				filter = new XssSaxFilter(XssSaxConfiguration.newInstance(fileName));
				filter.withoutComment = withoutComment;

				filter.service = filter.config.getService();
				filter.blockingPrefixEnabled = filter.config.isEnableBlockingPrefix();
				filter.blockingPrefix = filter.config.getBlockingPrefix();

				filter.filteringTagInCommentEnabled = filter.config.isFilteringTagInCommentEnabled();

				if (filter.filteringTagInCommentEnabled && ! filter.config.isNoTagAllowedInComment()) {

					filter.commentFilter = XssSaxFilter.getCommentFilterInstance(filter.config);

				}

				instanceMap.put(key, filter);
				return filter;
			}
		} catch (Exception e) {
			throw new XssFilterException(e.getMessage());
		}
	}

	public static XssSaxFilter getCommentFilterInstance(XssSaxConfiguration config) {

		XssSaxFilter filter = new XssSaxFilter(config);

		filter.service = filter.config.getService();
		filter.blockingPrefixEnabled = filter.config.isEnableBlockingPrefix();
		filter.blockingPrefix = filter.config.getBlockingPrefix();

		filter.withoutComment = true;

		filter.filteringTagInCommentEnabled = true;

		return filter;
	}

	public XssSaxConfiguration getConfig() {
		return this.config;
	}

	public String doFilter(String dirty) {
		StringWriter writer = new StringWriter();
		doFilter(dirty, writer);
		return writer.toString();
	}

	public void doFilter(String dirty, Writer writer) {
		StringWriter neloLogWriter = new StringWriter();

		if (dirty == null || dirty.length() == 0) {
			LOG.debug("target string is empty. doFilter() method end.");
			return;
		}

		try {
			this.parseAndFilter(dirty, writer, neloLogWriter);
		} catch (IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
		}

	}

	public void doFilter(char[] dirty, int offset, int count, Writer writer) {
		StringWriter neloLogWriter = new StringWriter();

		if (dirty == null || dirty.length == 0 || count == 0) {
			LOG.debug("target string is empty. doFilter() method end.");
			return;
		}

		try {
			this.parseAndFilter(dirty, offset, count, writer, neloLogWriter);
		} catch (IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
		}

	}

	private void parseAndFilter(String dirty, Writer writer, StringWriter neloLogWriter) throws IOException {
		if (dirty != null && dirty.length() > 0) {
			LinkedList<Element> stackForObjectTag = new LinkedList<Element>();
			LinkedList<String> stackForAllowNetworkingValue = new LinkedList<String>();

			CharArraySegment charArraySegment = new CharArraySegment(dirty);
			doParseAndFilter(writer, neloLogWriter, stackForObjectTag, stackForAllowNetworkingValue, charArraySegment);
		}
	}

	private void parseAndFilter(char[] dirty, int offset, int count, Writer writer, StringWriter neloLogWriter) throws IOException{
		if (dirty != null && dirty.length > 0 && count >0) {
			LinkedList<Element> stackForObjectTag = new LinkedList<Element>();
			LinkedList<String> stackForAllowNetworkingValue = new LinkedList<String>();

			CharArraySegment charArraySegment = new CharArraySegment(dirty, offset, count);
			doParseAndFilter(writer, neloLogWriter, stackForObjectTag, stackForAllowNetworkingValue, charArraySegment);
		}
	}

	private void doParseAndFilter(Writer writer, StringWriter neloLogWriter, LinkedList<Element> stackForObjectTag, LinkedList<String> stackForAllowNetworkingValue, CharArraySegment charArraySegment) throws IOException {
		Token token;
		while ((token = MarkupSaxParser.parse(charArraySegment)) != null) {
			String tokenName = token.getName();

			if ("description".equals(tokenName)) {

				String description = token.getText();
				Description content = new Description(description);
				content.serialize(writer);

			} else if ("comment".equals(tokenName)) {
				String comment = token.getText();
				if (comment != null && comment.length() != 0) {
					comment = comment.substring(4, comment.length() - 3);
				}

				Comment content = new Comment(comment);
				content.serializeFilteringTagInComment(writer, this.filteringTagInCommentEnabled, this.commentFilter);

			} else if ("iEHExStartTag".endsWith(tokenName)) {
				IEHackExtensionElement iehackElement = new IEHackExtensionElement(token.getText());
				checkIEHackRule(iehackElement);

				if (iehackElement.isDisabled()) {
				
					if (!this.withoutComment) {
						writer.write(REMOVE_TAG_INFO_START);
						writer.write(iehackElement.getName().replaceAll("<", "&lt;").replaceFirst(">", "&gt;"));
						writer.write(REMOVE_TAG_INFO_END);
					}
				}

				iehackElement.serialize(writer);

			} else if ("startTag".equals(tokenName)) {
				Token tagNameToken = token.getChild("tagName");
				if (tagNameToken == null) {
					continue;
				}

				String tagName = tagNameToken.getText();
				Element element = new Element(tagName);
				List<Token> attTokens = token.getChildren("attribute");
				if (attTokens != null) {
					for (Token attToken : attTokens) {
						if (attToken != null) {
							Token attName = attToken.getChild("attName");
							Token attValue = attToken.getChild("attValue");
							if (attName != null && attValue == null) {
								element.putAttribute(new Attribute(attName.getText()));
							} else if (attName != null && attValue != null) {
								String text = attValue.getText();
								text = CommonUtils.getQuotePair(text);
								element.putAttribute(new Attribute(attName.getText(), text));
							}
						}
					}
				}

				Token closeStartEnd = token.getChild("closeStartEnd");

				if (closeStartEnd != null) {
					element.setStartClose(true);

				}

				doObjectParamStartTagProcess(stackForObjectTag, stackForAllowNetworkingValue, element);

				this.serialize(writer, element, neloLogWriter);

			} else if ("iEHExEndTag".endsWith(tokenName)) {
				IEHackExtensionElement ie = new IEHackExtensionElement(token.getText());
				checkIEHackRule(ie);

				if (!ie.isDisabled()) {

					String stdName = ie.getName();
					if (stdName != null) {
						stdName = stdName.replaceFirst("<!--", "<!");
					}
					writer.write(stdName);
				}
			} else if ("endTag".equals(tokenName)) {
				Token tagNameToken = token.getChild("tagName");

				if (tagNameToken == null) {
					continue;
				}

				String tagName = tagNameToken.getText();

				boolean isObjectDisabled = false;
				if ("object".equalsIgnoreCase(tagName) && stackForObjectTag.size() > 0) {
					isObjectDisabled = doObjectEndTagProcess(writer, neloLogWriter, stackForObjectTag, stackForAllowNetworkingValue);

				}

				Element element = new Element(tagName);

				checkRuleRemove(element);

				if (!element.isRemoved()) {
					if (isObjectDisabled) {
						element.setEnabled(false);
					}

					if (!element.isDisabled() || this.blockingPrefixEnabled) {
						checkRule(element);
					}

					if (element.isDisabled()) {
						if (this.blockingPrefixEnabled) {
							element.setName(this.blockingPrefix + element.getName());
							element.setEnabled(true);
							writer.write("</");
							writer.write(element.getName());
							writer.write('>');
						} else {
							writer.write("&lt;/");
							writer.write(element.getName());
							writer.write("&gt;");
						}
					} else {
						writer.write("</");
						writer.write(element.getName());
						writer.write('>');
					}
				}
			} else {
				Text content = new Text(token.getText());
				content.serialize(writer);
			}
		}
	}

	private void doObjectParamStartTagProcess(LinkedList<Element> stackForObjectTag, LinkedList<String> stackForAllowNetworkingValue, Element element) {

		if ("object".equalsIgnoreCase(element.getName())) {
			stackForObjectTag.push(element);
			boolean isDataWhiteUrl = false;

			Attribute dataUrl = element.getAttribute("data");

			if (dataUrl != null) {
				String dataUrlStr = dataUrl.getValue();
				isDataWhiteUrl = this.isWhiteUrl(dataUrlStr);

				boolean isVulnerable = SecurityUtils.checkVulnerable(element, dataUrlStr, isDataWhiteUrl);

				if (isVulnerable) {
					element.setEnabled(false);
					return;
				}
			}

			if (isDataWhiteUrl) {
				stackForAllowNetworkingValue.push("\"all\"");
			} else {
				stackForAllowNetworkingValue.push("\"internal\"");
			}
		} else if (stackForObjectTag.size() > 0 && "param".equalsIgnoreCase(element.getName())) {
			Attribute nameAttr = element.getAttribute("name");
			Attribute valueAttr = element.getAttribute("value");

			if (nameAttr != null && valueAttr != null) {
				stackForObjectTag.push(element);
				if (containsURLName(nameAttr.getValue())) {
					stackForAllowNetworkingValue.pop();
					boolean whiteUrl = isWhiteUrl(valueAttr.getValue());

					if (whiteUrl) {
						stackForAllowNetworkingValue.push("\"all\"");
					} else {
						stackForAllowNetworkingValue.push("\"internal\"");
					}
				}
			}
		}
	}

	private boolean doObjectEndTagProcess(Writer writer, StringWriter neloLogWriter, LinkedList<Element> stackForObjectTag, LinkedList<String> stackForAllowNetworkingValue) throws IOException {
		List<String> paramNameList = new ArrayList<String>();

		Element item = null;

		while (stackForObjectTag.size() > 0) {
			item = stackForObjectTag.pop();
			if ("object".equalsIgnoreCase(item.getName())) {
				break;
			} else {
				Attribute nameAttr = item.getAttribute("name");
				if (nameAttr != null) {
					paramNameList.add(nameAttr.getValue());
				}
			}

		}

		if (item == null || !"object".equalsIgnoreCase(item.getName())) {
			return false;
		} else if (item != null && item.isDisabled()) {
			return true;
		}

		for (int index = 0; index < PARAMLIST.length; index++) {
			Pattern pattern = PARAMLIST[index];

			boolean exist = false;
			for (String paramName : paramNameList) {
				if (pattern.matcher(paramName).matches()) {
					exist = true;
					break;
				}
			}

			if (!exist) {

				switch (index) {

					case 0:
						Element invokeURLs = new Element("param");
						invokeURLs.putAttribute("name", "\"invokeURLs\"");
						invokeURLs.putAttribute("value", "\"false\"");
						this.serialize(writer, invokeURLs, neloLogWriter);
						break;

					case 1:
						Element autostart = new Element("param");
						autostart.putAttribute("name", "\"autostart\"");
						autostart.putAttribute("value", "\"false\"");
						this.serialize(writer, autostart, neloLogWriter);
						break;

					case 2:
						Element allowScriptAccess = new Element("param");
						allowScriptAccess.putAttribute("name", "\"allowScriptAccess\"");
						allowScriptAccess.putAttribute("value", "\"never\"");
						this.serialize(writer, allowScriptAccess, neloLogWriter);
						break;

					case 3:
						Element allowNetworking = new Element("param");
						allowNetworking.putAttribute("name", "\"allowNetworking\"");
						allowNetworking.putAttribute("value", stackForAllowNetworkingValue.size() == 0 ? "\"internal\"" : stackForAllowNetworkingValue.pop());
						this.serialize(writer, allowNetworking, neloLogWriter);
						break;

					case 4:
						Element autoplay = new Element("param");
						autoplay.putAttribute("name", "\"autoplay\"");
						autoplay.putAttribute("value", "\"false\"");
						this.serialize(writer, autoplay, neloLogWriter);
						break;

					case 5:
						Element enablehref = new Element("param");
						enablehref.putAttribute("name", "\"enablehref\"");
						enablehref.putAttribute("value", "\"false\"");
						this.serialize(writer, enablehref, neloLogWriter);
						break;

					case 6:
						Element enablejavascript = new Element("param");
						enablejavascript.putAttribute("name", "\"enablejavascript\"");
						enablejavascript.putAttribute("value", "\"false\"");
						this.serialize(writer, enablejavascript, neloLogWriter);
						break;

					case 7:
						Element nojava = new Element("param");
						nojava.putAttribute("name", "\"nojava\"");
						nojava.putAttribute("value", "\"true\"");
						this.serialize(writer, nojava, neloLogWriter);
						break;

					case 8:
						Element allowHtmlPopupwindow = new Element("param");
						allowHtmlPopupwindow.putAttribute("name", "\"AllowHtmlPopupwindow\"");
						allowHtmlPopupwindow.putAttribute("value", "\"false\"");
						this.serialize(writer, allowHtmlPopupwindow, neloLogWriter);
						break;

					case 9:
						Element enableHtmlAccess = new Element("param");
						enableHtmlAccess.putAttribute("name", "\"enableHtmlAccess\"");
						enableHtmlAccess.putAttribute("value", "\"false\"");
						this.serialize(writer, enableHtmlAccess, neloLogWriter);
						break;
					default:
						System.out.println("test needs to be added test Endpoint.");
				}
			}
		}

		return false;
	}

	private void serialize(Writer writer, IEHackExtensionElement ie, StringWriter neloLogWriter) throws IOException {
		checkIEHackRule(ie);

		if (ie.isDisabled()) {
			
			if (!this.withoutComment) {
				writer.write(REMOVE_TAG_INFO_START);
				writer.write(ie.getName().replaceAll("<", "&lt;").replaceFirst(">", "&gt;"));
				writer.write(REMOVE_TAG_INFO_END);
			}
		} else {

			String stdName = ie.getName().replaceAll("-->", ">").replaceFirst("<!--\\s*", "<!--").replaceAll("]\\s*>", "]>");

			int startIndex = stdName.indexOf("<!") + 1;
			int lastIntndex = stdName.lastIndexOf(">");

			String firststdName = stdName.substring(0, startIndex);
			String middlestdName = StringUtils.replaceEach(stdName.substring(startIndex, lastIntndex), new String[] {"<", ">"}, new String[] {"&lt;", "&gt;"});
			String laststdName = stdName.substring(lastIntndex);

			stdName = firststdName + middlestdName + laststdName;

			writer.write(stdName);

		}
	}

	private void checkIEHackRule(IEHackExtensionElement ie) {
		ElementRule iEHExRule = this.config.getElementRule(IE_HACK_EXTENSION);

		if (iEHExRule != null) {

			iEHExRule.checkDisabled(ie);
			iEHExRule.excuteListener(ie);
		} else {
			ie.setEnabled(false);
		}
	}

	private void serialize(Writer writer, Element element, StringWriter neloLogWriter) throws IOException {
		boolean hasAttrXss = false;
		checkRuleRemove(element);

		if (element.isRemoved()) {

			if (!this.withoutComment) {
				writer.write(REMOVE_TAG_INFO_START);
				writer.write(element.getName());
				writer.write(REMOVE_TAG_INFO_END);
			}
		} else {

			if (!element.isDisabled() || this.blockingPrefixEnabled) {
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

		tagRule.checkDisabled(element);

		Collection<Attribute> atts = element.getAttributes();
		if (atts != null && !atts.isEmpty()) {
			for (Attribute att : atts) {
				if (att.isDisabled()) {
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
