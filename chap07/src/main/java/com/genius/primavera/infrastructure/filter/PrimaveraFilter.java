package com.genius.primavera.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Slf4j
@ToString
@Component
public class PrimaveraFilter extends OncePerRequestFilter {

	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (antPathMatcher.match("/login", request.getRequestURI())) {
			response.setHeader("primavera", "filter");
			if(request.getMethod().equals("POST")) {
				var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(request.getParameter("email"), request.getParameter("password"));
				SecurityContext securityContext =  SecurityContextHolder.getContext();
				securityContext.setAuthentication(usernamePasswordAuthenticationToken);
				HttpSession session = request.getSession(true);
				session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext);
			}
		}
		filterChain.doFilter(request, response);
	}
}
