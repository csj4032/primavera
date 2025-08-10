

package com.navercorp.lucy.security.xss.servletfilter;

import com.navercorp.lucy.security.xss.servletfilter.defender.Defender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class XssEscapeFilterConfig {
	private static final String DEFAULT_FILTER_RULE_FILENAME = "lucy-xss-servlet-filter-rule.xml";

	private static final Log LOG = LogFactory.getLog(XssEscapeFilterConfig.class);
	
	private Map<String, Map<String, XssEscapeFilterRule>> urlRuleSetMap = new HashMap<String, Map<String, XssEscapeFilterRule>>();
	private Map<String, XssEscapeFilterRule> globalParamRuleMap = new HashMap<String, XssEscapeFilterRule>();
	private Map<String, Defender> defenderMap = new HashMap<String, Defender>();
	private Defender defaultDefender = null;

	public XssEscapeFilterConfig() throws IllegalStateException {
		this(DEFAULT_FILTER_RULE_FILENAME);
	}

	public XssEscapeFilterConfig(String filename) throws IllegalStateException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
			Element rootElement = builder.parse(is).getDocumentElement();

			addDefenders(rootElement);

			addDefaultInfo(rootElement);

			addGlobalParams(rootElement);

			addUrlRuleSet(rootElement);

		} catch (Exception e) {
			String message = String.format("Cannot parse the RequestParam configuration file [%s].", filename);
			throw new IllegalStateException(message, e);
		}
	}

	private void addDefaultInfo(Element rootElement) {
		NodeList nodeList = rootElement.getElementsByTagName("default");
		if (nodeList.getLength() > 0) {
			Element element = (Element)nodeList.item(0);
			addDefaultInfoItems(element);
		}
	}

	private void addDefaultInfoItems(Element element) {
		NodeList nodeList = element.getElementsByTagName("defender");
		if (nodeList.getLength() > 0) {
			defaultDefender = defenderMap.get(nodeList.item(0).getTextContent());
			
			if (defaultDefender == null) {
				LOG.error("Error config 'Default defender': Not found '" + nodeList.item(0).getTextContent() + "'");
			}
		}
	}

	private void addGlobalParams(Element rootElement) {
		NodeList nodeList = rootElement.getElementsByTagName("global");
		if (nodeList.getLength() > 0) {
			Element params = (Element)nodeList.item(0);
			NodeList paramNodeList = params.getElementsByTagName("params");
			
			if (paramNodeList.getLength() > 0) {
				globalParamRuleMap = createRequestParamRuleMap((Element)nodeList.item(0));
			}			
		}
	}

	private void addUrlRuleSet(Element rootElement) {
		NodeList nodeList = rootElement.getElementsByTagName("url-rule");
		for (int i = 0; nodeList.getLength() > 0 && i < nodeList.getLength(); i++) {
			Element element = (Element)nodeList.item(i);
			addUrlRule(element);
		}
	}

	private void addUrlRule(Element element) {
		Map<String, XssEscapeFilterRule> paramRuleMap = null;
		String url = null;
		
		NodeList nodeList = element.getElementsByTagName("url");
		if (nodeList.getLength() > 0) {
			url = nodeList.item(0).getTextContent();

			if (addUrlDisableRule(url, nodeList)) {
				return;
			}
		}
		
		nodeList = element.getElementsByTagName("params");
		if (nodeList.getLength() > 0) {
			paramRuleMap = createRequestParamRuleMap((Element)nodeList.item(0));
		}

		urlRuleSetMap.put(url, paramRuleMap);
	}

	private boolean addUrlDisableRule(String url, NodeList nodeList) {
		Map<String, XssEscapeFilterRule> paramRuleMap = null;
		boolean result = false;
		
		if (!url.isEmpty()) {
			boolean disable = StringUtils.equalsIgnoreCase(((Element)nodeList.item(0)).getAttribute("disable"), "true") ? true : false;
			paramRuleMap = createRequestParamRuleMap(url, disable);
			
			if (paramRuleMap != null) {
				urlRuleSetMap.put(url, paramRuleMap);
				result = true;
			}
		}
		
		return result;
	}

	private Map<String, XssEscapeFilterRule> createRequestParamRuleMap(Element element) {
		Map<String, XssEscapeFilterRule> urlRuleMap = new HashMap<String, XssEscapeFilterRule>();

		NodeList nodeList = element.getElementsByTagName("param");
		for (int i = 0; nodeList.getLength() > 0 && i < nodeList.getLength(); i++) {
			Element eachElement = (Element)nodeList.item(i);
			String name = eachElement.getAttribute("name");
			boolean useDefender = StringUtils.equalsIgnoreCase(eachElement.getAttribute("useDefender"), "false") ? false : true;
			boolean usePrefix = StringUtils.equalsIgnoreCase(eachElement.getAttribute("usePrefix"), "true") ? true : false;
			Defender defender = null;

			NodeList defenderNodeList = eachElement.getElementsByTagName("defender");
			if (defenderNodeList.getLength() > 0) {
				defender = defenderMap.get(defenderNodeList.item(0).getTextContent());
				
				if (defender == null) {
					LOG.error("Error config 'param defender': Not found '" + nodeList.item(0).getTextContent() + "'");
				}
			} else {
				defender = defaultDefender;
			}

			XssEscapeFilterRule urlRule = new XssEscapeFilterRule();
			urlRule.setName(name);
			urlRule.setUseDefender(useDefender);
			urlRule.setDefender(defender);
			urlRule.setUsePrefix(usePrefix);

			urlRuleMap.put(name, urlRule);
		}

		return urlRuleMap;
	}

	private Map<String, XssEscapeFilterRule> createRequestParamRuleMap(String url, boolean disable) {
		if (!disable) {
			return null;
		}
		
		Map<String, XssEscapeFilterRule> urlRuleMap = new HashMap<String, XssEscapeFilterRule>();
		XssEscapeFilterRule urlRule = new XssEscapeFilterRule();
		urlRule.setName(url);
		urlRule.setUseDefender(false);
		urlRuleMap.put(url, urlRule);
		
		return urlRuleMap;
	}

	private void addDefenders(Element rootElement) {
		NodeList nodeList = rootElement.getElementsByTagName("defenders");

		if (nodeList.getLength() > 0) {
			Element element = (Element)nodeList.item(0);
			addDefender(element);
		}
	}

	private void addDefender(Element element) {
		NodeList nodeList = element.getElementsByTagName("defender");
		for (int i = 0; nodeList.getLength() > 0 && i < nodeList.getLength(); i++) {
			Element eachElement = (Element)nodeList.item(i);
			String name = getTagContent(eachElement, "name");
			String clazz = getTagContent(eachElement, "class");
			String[] args = getInitParams(eachElement);
			addDefender(name, clazz, args);
		}
	}

	private void addDefender(String name, String clazz, String[] args) {

		if (StringUtils.isBlank(name) || StringUtils.isBlank(clazz)) {
			String message = String.format("The defender's name('%s') or clazz('%s') is empty. This defender is ignored", name, clazz);
			LOG.warn(message);
			return;
		}
		try {
			Defender defender = (Defender)Class.forName(clazz.trim()).newInstance();
			defender.init(args);
			defenderMap.put(name, defender);
		} catch (InstantiationException e) {
			rethrow(name, clazz, e);
		} catch (IllegalAccessException e) {
			rethrow(name, clazz, e);
		} catch (ClassNotFoundException e) {
			rethrow(name, clazz, e);

		}
	}

	private void rethrow(String name, String clazz, Exception e) {
		String message = String.format("Fail to add defender: name=%s, class=%s", name, clazz);
		throw new IllegalStateException(message, e);
	}

	private String[] getInitParams(Element eachElement) {
		NodeList initParamNodeList = eachElement.getElementsByTagName("init-param");
		if (initParamNodeList.getLength() == 0) {
			return new String[]{};
		}
		Element paramValueElement = (Element)initParamNodeList.item(0);
		NodeList paramValueNodeList = paramValueElement.getElementsByTagName("param-value");
	
		String[] args = new String[paramValueNodeList.getLength()];
		for (int j = 0; paramValueNodeList.getLength() > 0 && j < paramValueNodeList.getLength(); j++) {
			args[j] = paramValueNodeList.item(j).getTextContent();
		}
		return args;
	}

	private String getTagContent(Element eachElement, String tagName) {
		NodeList nodeList = eachElement.getElementsByTagName(tagName);
		if (nodeList.getLength() > 0) {
			return nodeList.item(0).getTextContent();
		}
		return "";
	}

	public XssEscapeFilterRule getUrlParamRule(String url, String paramName) {
		Map<String, XssEscapeFilterRule> urlParamRuleMap = urlRuleSetMap.get(url);
		XssEscapeFilterRule paramRule = null;
		
		if (urlParamRuleMap == null) {
			paramRule = checkGlobalParamRule(paramName);
		} else {

			paramRule = checkParamRule(urlParamRuleMap, url, paramName);
		}
		
		return paramRule;
	}

	private XssEscapeFilterRule checkGlobalParamRule(String paramName) {
		XssEscapeFilterRule paramRule = globalParamRuleMap.get(paramName);

		if (paramRule == null) {
			paramRule = checkPrefixParameter(paramName, null, globalParamRuleMap);
		}
		
		return paramRule;
	}

	private XssEscapeFilterRule checkParamRule(Map<String, XssEscapeFilterRule> urlParamRuleMap, String url, String paramName) {
		XssEscapeFilterRule paramRule = urlParamRuleMap.get(paramName);
		
		if (paramRule == null) {

			paramRule = checkDisableUrl(url, paramRule, urlParamRuleMap);

			paramRule = checkPrefixParameter(paramName, paramRule, urlParamRuleMap);
			
			if (paramRule == null) {
				paramRule = globalParamRuleMap.get(paramName);
			}
		}
		return paramRule;
	}

	private XssEscapeFilterRule checkDisableUrl(String url, XssEscapeFilterRule paramRule, Map<String, XssEscapeFilterRule> urlParamRuleMap) {
		if (paramRule != null) {
			return paramRule;
		}
		
		if (urlParamRuleMap.containsKey(url) && !(urlParamRuleMap.get(url).isUseDefender())) {
			return urlParamRuleMap.get(url);
		}
		return paramRule;
	}

	private XssEscapeFilterRule checkPrefixParameter(String paramName, XssEscapeFilterRule paramRule, Map<String, XssEscapeFilterRule> urlParamRuleMap) {
		if (paramRule != null || paramName == null) {
			return paramRule;
		}
		
		Set<Entry<String, XssEscapeFilterRule>> entries = urlParamRuleMap.entrySet();
		for (Entry<String, XssEscapeFilterRule> entry : entries) {
			if (entry.getValue().isUsePrefix() && paramName.startsWith(entry.getKey())) {
				return urlParamRuleMap.get(entry.getKey());
			} 
		}
		return paramRule;
	}

	public Map<String, Defender> getDefenderMap() {
		return defenderMap;
	}

	public Defender getDefaultDefender() {
		return defaultDefender;
	}
}
