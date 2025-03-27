package com.navercorp.lucy.security.xss.servletfilter;

import jakarta.servlet.*;

import java.io.IOException;

public class XssEscapeServletFilter implements Filter {

    private XssEscapeFilter xssEscapeFilter = XssEscapeFilter.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new XssEscapeServletFilterWrapper(request, xssEscapeFilter), response);
    }

    @Override
    public void destroy() {
    }
}
