package com.genius.primavera.infrastructure.security;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;

public class PrimaveraAuthSuccessApplicationListener implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {

    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        System.out.println("PrimaveraAuthSuccessApplicationListener");
    }
}