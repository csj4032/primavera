package com.genius.primavera.infrastructure.security;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class ClientResources {

    @NestedConfigurationProperty
    private OAuth2ClientProperties client = new OAuth2ClientProperties();

    @NestedConfigurationProperty  
    private OAuth2ResourceProperties resource = new OAuth2ResourceProperties();

    public OAuth2ClientProperties getClient() {
        return client;
    }

    public OAuth2ResourceProperties getResource() {
        return resource;
    }

    public static class OAuth2ClientProperties {
        private String clientId;
        private String clientSecret;
        private String accessTokenUri;
        private String userAuthorizationUri;
        private String scope;

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        
        public String getAccessTokenUri() { return accessTokenUri; }
        public void setAccessTokenUri(String accessTokenUri) { this.accessTokenUri = accessTokenUri; }
        
        public String getUserAuthorizationUri() { return userAuthorizationUri; }
        public void setUserAuthorizationUri(String userAuthorizationUri) { this.userAuthorizationUri = userAuthorizationUri; }
        
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }

    public static class OAuth2ResourceProperties {
        private String userInfoUri;
        private String tokenInfoUri;

        public String getUserInfoUri() { return userInfoUri; }
        public void setUserInfoUri(String userInfoUri) { this.userInfoUri = userInfoUri; }
        
        public String getTokenInfoUri() { return tokenInfoUri; }
        public void setTokenInfoUri(String tokenInfoUri) { this.tokenInfoUri = tokenInfoUri; }
    }
}
