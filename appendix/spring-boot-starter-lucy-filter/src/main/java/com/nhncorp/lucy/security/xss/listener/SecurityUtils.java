	
package com.nhncorp.lucy.security.xss.listener;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.nhncorp.lucy.security.xss.Constants;
import com.nhncorp.lucy.security.xss.markup.Element;

public class SecurityUtils {
	
	private static final String EXTENSION_PROPERTIES = "xssfilter-extension.properties";
	private final static char[] specialCharArray = "?&=".toCharArray();
	 
    public static final char EXTENSION_SEPARATOR = '.';

    private static final char UNIX_SEPARATOR = '/';

    private static final char WINDOWS_SEPARATOR = '\\';
    
    private static Properties props;
    
    static {
    	try {
			props = new Properties();
			props.load(SecurityUtils.class.getClassLoader().getResourceAsStream(EXTENSION_PROPERTIES));
		} catch (Exception e) {
			System.out.println(EXTENSION_PROPERTIES + " connection test Endpoint.");
		}
    }

	public static boolean checkVulnerable(Element element, String srcUrl, boolean isWhiteUrl) {
		boolean isVulnerable = false;

		if (isWhiteUrl) {

		} else {
			String type = element.getAttributeValue("type").trim();
			type = StringUtils.strip(type, "'\"");

			if (type != null && type.length() != 0) {

				if (!(isAllowedType(type) || props.values().contains(type))) {
					isVulnerable = true;
				}
			} else {

				String url = StringUtils.strip(srcUrl, "'\"");
				String extension = getExtension(url);
				
				if (StringUtils.containsAny(extension, specialCharArray)) {
					int pos = StringUtils.indexOfAny(extension, specialCharArray);
					if (pos != -1) {
						extension = StringUtils.substring(extension, 0, pos);
					}
				}
				
				if (StringUtils.isEmpty(extension)) {

				} else {
					type = getTypeFromExtension(extension);
					
					if (StringUtils.isEmpty(type)) {
						type = props.getProperty(extension);
						
						if(type!=null) {
							type = type.trim();
						}
					}

					if (StringUtils.isEmpty(type)) {
						isVulnerable = true;
					} else {
						element.putAttribute("type", "\"" + type + "\"");
					}
				}

			}
		}
		return isVulnerable;
	}

	public static boolean checkVulnerableWithHttp(Element element, String srcUrl, boolean isWhiteUrl, ContentTypeCacheRepo contentTypeCacheRepo) {
		boolean isVulnerable = false;

		if (isWhiteUrl) {

		} else {
			String type = element.getAttributeValue("type").trim();
			type = StringUtils.strip(type, "'\"");

			if (type != null && !"".equals(type)) {

				if (!(isAllowedType(type) || props.values().contains(type))) {
					isVulnerable = true;
				}
			} else {

				String url = StringUtils.strip(srcUrl, "'\"");
				String extension = getExtension(url);
				
				if (StringUtils.containsAny(extension, specialCharArray)) {
					int pos = StringUtils.indexOfAny(extension, specialCharArray);
					if (pos != -1) {
						extension = StringUtils.substring(extension, 0, pos);
					}
				}

				if (StringUtils.isEmpty(extension)) {

					type = "application/octet-stream";

					if (!isAllowedType(type)) {
						isVulnerable = true;
					} else {
						element.putAttribute("type", "\"" + type + "\"");
					}

				} else {
					type = getTypeFromExtension(extension);
					
					if (StringUtils.isEmpty(type)) {
						type = props.getProperty(extension);
						
						if(type!=null) {
							type = type.trim();
						}
					}

					if (StringUtils.isEmpty(type)) {
						isVulnerable = true;
					} else {
						element.putAttribute("type", "\"" + type + "\"");
					}
				}

			}
		}
		return isVulnerable;
	}

	public static String getContentTypeFromUrlConnection(String strUrl, ContentTypeCacheRepo contentTypeCacheRepo) {

		String result = contentTypeCacheRepo.getContentTypeFromCache(strUrl);

		if (StringUtils.isNotEmpty(result)) {
			return result;
		}

		HttpURLConnection con = null;

		try {
			URL url = new URL(strUrl);
			con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("HEAD");
			con.setConnectTimeout(1000);
			con.setReadTimeout(1000);
			con.connect();

			int resCode = con.getResponseCode();

			if (resCode != HttpURLConnection.HTTP_OK) {
				System.err.println("error");
			} else {
				result = con.getContentType();

				if (result != null) {
					contentTypeCacheRepo.addContentTypeToCache(strUrl, new ContentType(result, new Date()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		return result;

	}

	public static String getTypeFromExtension(String extension) {
		return Constants.mimeTypes.get(extension);
	}

	public static boolean isAllowedType(String type) {

		if (StringUtils.isEmpty(type)) {
			return false;
		} else if (StringUtils.startsWith(type, "text/")) {
			return false;

		} else if (StringUtils.isNotEmpty(type) && !Constants.mimeTypes.values().contains(type)) {
			return false;
		} else {
			return true;
		}
	}

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = indexOfLastSeparator(filename);
        return (lastSeparator > extensionPos ? -1 : extensionPos);
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }
}
