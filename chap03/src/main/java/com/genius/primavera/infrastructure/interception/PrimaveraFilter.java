package com.genius.primavera.infrastructure.interception;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
