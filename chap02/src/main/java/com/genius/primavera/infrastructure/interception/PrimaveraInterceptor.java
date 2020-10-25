package com.genius.primavera.infrastructure.interception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Enumeration;

@Slf4j
@Component
public class PrimaveraInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		log.info("preHandler {}, {}", this, request);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		log.info("postHandler");
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
		if (response instanceof ResettableStreamHttpServletResponse) {
			//((ResettableStreamHttpServletResponse) response).payloadFilePrefix = ((ResettableStreamHttpServletRequest) request).payloadFilePrefix;
			//((ResettableStreamHttpServletResponse) response).payloadTarget = ((ResettableStreamHttpServletRequest) request).payloadTarget;
			//writeResponsePayloadAudit((ResettableStreamHttpServletResponse) response);
		}
		log.info("afterCompletion");
	}

	public void writeRequestPayloadAudit(ResettableStreamHttpServletRequest wrappedRequest) {
		try {
			String requestHeaders = getRawHeaders(wrappedRequest);
			String requestBody = org.apache.commons.io.IOUtils.toString(wrappedRequest.getReader());
			log.info("Request Method: " + wrappedRequest.getMethod());
			log.info("Request Headers:");
			log.info(requestHeaders);
			log.info("Request body:");
			log.info(requestBody);
		} catch (Exception e) {
			log.error("{}", e);
		}
	}

	public String getRawHeaders(HttpServletRequest request) {
		StringBuffer rawHeaders = new StringBuffer();
		Enumeration headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			rawHeaders.append(key).append(":").append(value).append("\n");
		}

		return rawHeaders.toString();
	}

	public String getRawHeaders(HttpServletResponse response) {
		StringBuffer rawHeaders = new StringBuffer();
		Enumeration headerNames = Collections.enumeration(response.getHeaderNames());
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = response.getHeader(key);
			rawHeaders.append(key).append(":").append(value).append("\n");
		}
		return rawHeaders.toString().trim();
	}

	public void writeResponsePayloadAudit(ResettableStreamHttpServletResponse wrappedResponse) {
		String rawHeaders = getRawHeaders(wrappedResponse);
		log.info("Response Status: {}", wrappedResponse.getStatus());
		log.info("Response Headers: {}", rawHeaders);
		byte[] data = new byte[wrappedResponse.rawData.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = wrappedResponse.rawData.get(i);
		}
		String responseBody = new String(data);
		log.info("Response Body : \n{}", responseBody);
	}
}

