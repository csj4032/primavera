package com.genius.primavera.interfaces;

import com.genius.primavera.infrastructure.security.PrimaveraUserDetails;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockPrimaveraUserSecurityContextFactory implements WithSecurityContextFactory<WithMockPrimaveraUserDetails> {

    @Autowired
    private PrimaveraUserDetailsService primaveraUserDetailsService;

    @Override
    public SecurityContext createSecurityContext(WithMockPrimaveraUserDetails primaveraUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        PrimaveraUserDetails principal = (PrimaveraUserDetails) primaveraUserDetailsService.loadUserByUsername(primaveraUser.username());
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}