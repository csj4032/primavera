package com.genius.primavera.infrastructure.interception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrimaveraFilter implements Filter {

	private final PrimaveraInterceptor primaveraInterceptor;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		log.info("doFilter, parsing request");
		ResettableStreamHttpServletRequest wrappedRequest = null;
		ResettableStreamHttpServletResponse wrappedResponse = null;
		try {
			wrappedRequest = new ResettableStreamHttpServletRequest((HttpServletRequest) request);
			wrappedResponse = new ResettableStreamHttpServletResponse((HttpServletResponse) response);
			primaveraInterceptor.writeRequestPayloadAudit(wrappedRequest);
		} catch (Exception e) {
			log.error("Fail to wrap request and response", e);
		}
		chain.doFilter(wrappedRequest, wrappedResponse);
	}
}
