package com.genius.primavera.infrastructure.security;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class ClientResources {

    @NestedConfigurationProperty
    private ClientRegistration.Builder client;

    @NestedConfigurationProperty  
    private String resourceId;

    public ClientRegistration.Builder getClient() {
        return client;
    }

    public void setClient(ClientRegistration.Builder client) {
        this.client = client;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
