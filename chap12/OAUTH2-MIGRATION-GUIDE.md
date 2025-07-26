# OAuth2 Migration Guide for Spring Boot 3.x

## Overview
This guide documents the migration of OAuth2 social login functionality from deprecated Spring Security OAuth2 (Spring Boot 2.x) to the new Spring Security OAuth2 Client (Spring Boot 3.x).

## Deprecated Classes Moved to `disabled-oauth2-code/`

The following OAuth2-related classes have been temporarily disabled as they use deprecated APIs:

### Social Login Infrastructure
- `PrimaveraSocialConfiguration.java` - Main OAuth2 configuration class
- `ClientResources.java` - OAuth2 client resource configuration
- `SocialAuthentication.java` - Social authentication interface
- `SocialUserDetails.java` - Base social user details interface

### Provider-Specific Implementations
- `facebook/FacebookUserDetails.java`
- `facebook/FacebookOAuth2ClientAuthenticationProcessingFilter.java`
- `github/GithubUserDetails.java` 
- `github/GithubOAuth2ClientAuthenticationProcessingFilter.java`
- `google/GoogleUserDetails.java`
- `google/GoogleOAuth2ClientAuthenticationProcessingFilter.java`

## Migration Changes Made

### 1. Security Configuration
- Migrated from `WebSecurityConfigurerAdapter` to `SecurityFilterChain` approach
- Removed OAuth2 SSO filter temporarily from security chain
- Updated to use `@EnableMethodSecurity` instead of `@EnableGlobalMethodSecurity`

### 2. Dependencies Updated
- Added `spring-boot-starter-validation` for jakarta.validation support
- Changed to `spring-security-oauth2-client` and `spring-security-oauth2-jose`
- Removed deprecated `spring-security-oauth2-autoconfigure`

### 3. Code Changes
- Commented out OAuth2-dependent methods in `UserConnection.java`
- Removed JetBrains `@NotNull` annotations (replaced with method contracts)
- Temporarily disabled `XssEscapeServletFilter` (javax.servlet vs jakarta.servlet issue)

## Deprecated API Mappings

### Authentication Filter
**Old (Spring Boot 2.x):**
```java
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
```

**New (Spring Boot 3.x):**
```java
// Use OAuth2LoginAuthenticationFilter or custom implementation
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
```

### OAuth2 Access Token
**Old:**
```java
import org.springframework.security.oauth2.common.OAuth2AccessToken;
```

**New:**
```java
import org.springframework.security.oauth2.core.OAuth2AccessToken;
```

### Client Configuration
**Old:**
```java
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
```

**New:**
```java
// Use application.yml configuration with ClientRegistration
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-client-id
            client-secret: your-client-secret
```

## Migration TODO

### Phase 1: Basic OAuth2 Client Setup
1. Configure OAuth2 client registrations in `application.yml`
2. Replace deprecated authentication filters with standard OAuth2LoginAuthenticationFilter
3. Implement OAuth2User to UserDetails mapping

### Phase 2: Social Login Integration
1. Create new OAuth2UserService implementations for each provider
2. Implement OAuth2AuthenticationSuccessHandler for user registration
3. Update UserConnection model to work with new OAuth2 tokens

### Phase 3: Custom User Details
1. Migrate provider-specific UserDetails classes
2. Implement OAuth2UserRequest to custom UserDetails mapping
3. Update social authentication flow

## References
- [Spring Security OAuth2 Client Migration Guide](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Spring Boot OAuth2 Client Starter](https://docs.spring.io/spring-boot/reference/web/spring-security.html#web.security.oauth2)
- [OAuth2 Login Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)

## Current Status
✅ **Compilation Fixed** - Project now compiles successfully with Spring Boot 3.x
⚠️ **OAuth2 Features Disabled** - Social login functionality temporarily unavailable
📋 **Tests Status** - Some tests failing due to database configuration issues (not related to OAuth2 migration)