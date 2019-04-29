## chap08

### build.gradle spring-security 추가
```
compile('org.springframework.security:spring-security-config:5.1.5.RELEASE')
compile('org.springframework.security:spring-security-web:5.1.5.RELEASE')
```

### Logging default.xml 변경
```
<logger name="org.hibernate.validator.internal.util.Version" level="ERROR"/>
<logger name="org.springframework.web" level="DEBUG"/>
<logger name="org.springframework.security" level="DEBUG"/>
```

### SecurityConfig 파일 추가

### DelegatingFilterProxy
* Proxy for a standard Servlet Filter, delegating to a Spring-managed bean that implements the Filter interface.

### Flow 
* org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
* org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

### FilterChainProxy log

```
2019-04-29 15:50:33.894 DEBUG 37625 --- [  XNIO-1 task-1] io.undertow.request.security             : Attempting to authenticate /, authentication required: false
2019-04-29 15:50:33.897 DEBUG 37625 --- [  XNIO-1 task-1] io.undertow.request.security             : Authentication outcome was NOT_ATTEMPTED with method io.undertow.security.impl.CachedAuthenticatedSessionMechanism@988e188 for /
2019-04-29 15:50:33.898 DEBUG 37625 --- [  XNIO-1 task-1] io.undertow.request.security             : Authentication result was ATTEMPTED for /
2019-04-29 15:50:33.900  INFO 37625 --- [  XNIO-1 task-1] io.undertow.servlet                      : Initializing Spring DispatcherServlet 'dispatcherServlet'
2019-04-29 15:50:33.901  INFO 37625 --- [  XNIO-1 task-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2019-04-29 15:50:33.913  INFO 37625 --- [  XNIO-1 task-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 12 ms
2019-04-29 15:50:33.925 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 1 of 15 in additional filter chain; firing Filter: 'WebAsyncManagerIntegrationFilter'
2019-04-29 15:50:33.925 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 2 of 15 in additional filter chain; firing Filter: 'SecurityContextPersistenceFilter'
2019-04-29 15:50:33.926 DEBUG 37625 --- [  XNIO-1 task-1] w.c.HttpSessionSecurityContextRepository : No HttpSession currently exists
2019-04-29 15:50:33.926 DEBUG 37625 --- [  XNIO-1 task-1] w.c.HttpSessionSecurityContextRepository : No SecurityContext was available from the HttpSession: null. A new one will be created.
2019-04-29 15:50:33.928 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 3 of 15 in additional filter chain; firing Filter: 'HeaderWriterFilter'
2019-04-29 15:50:33.929 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 4 of 15 in additional filter chain; firing Filter: 'CsrfFilter'
2019-04-29 15:50:33.930 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 5 of 15 in additional filter chain; firing Filter: 'LogoutFilter'
2019-04-29 15:50:33.930 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.u.matcher.AntPathRequestMatcher  : Request 'GET /' doesn't match 'POST /logout'
2019-04-29 15:50:33.930 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 6 of 15 in additional filter chain; firing Filter: 'UsernamePasswordAuthenticationFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.u.matcher.AntPathRequestMatcher  : Request 'GET /' doesn't match 'POST /login'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 7 of 15 in additional filter chain; firing Filter: 'DefaultLoginPageGeneratingFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 8 of 15 in additional filter chain; firing Filter: 'DefaultLogoutPageGeneratingFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.u.matcher.AntPathRequestMatcher  : Checking match of request : '/'; against '/logout'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 9 of 15 in additional filter chain; firing Filter: 'BasicAuthenticationFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 10 of 15 in additional filter chain; firing Filter: 'RequestCacheAwareFilter'
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.s.HttpSessionRequestCache        : saved request doesn't match
2019-04-29 15:50:33.931 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 11 of 15 in additional filter chain; firing Filter: 'SecurityContextHolderAwareRequestFilter'
2019-04-29 15:50:33.932 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 12 of 15 in additional filter chain; firing Filter: 'AnonymousAuthenticationFilter'
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.AnonymousAuthenticationFilter  : Populated SecurityContextHolder with anonymous token: 'org.springframework.security.authentication.AnonymousAuthenticationToken@284a7a90: Principal: anonymousUser; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@b364: RemoteIpAddress: 0:0:0:0:0:0:0:1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS'
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 13 of 15 in additional filter chain; firing Filter: 'SessionManagementFilter'
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.session.SessionManagementFilter  : Requested session ID gYaORwNBFNAPNla4SKJWVlxUD-w_qMVyd0dGLku6 is invalid.
2019-04-29 15:50:33.933 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 14 of 15 in additional filter chain; firing Filter: 'ExceptionTranslationFilter'
2019-04-29 15:50:33.934 DEBUG 37625 --- [  XNIO-1 task-1] o.s.security.web.FilterChainProxy        : / at position 15 of 15 in additional filter chain; firing Filter: 'FilterSecurityInterceptor'
2019-04-29 15:50:33.934 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.i.FilterSecurityInterceptor    : Secure object: FilterInvocation: URL: /; Attributes: [authenticated]
2019-04-29 15:50:33.934 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.i.FilterSecurityInterceptor    : Previously Authenticated: org.springframework.security.authentication.AnonymousAuthenticationToken@284a7a90: Principal: anonymousUser; Credentials: [PROTECTED]; Authenticated: true; Details: org.springframework.security.web.authentication.WebAuthenticationDetails@b364: RemoteIpAddress: 0:0:0:0:0:0:0:1; SessionId: null; Granted Authorities: ROLE_ANONYMOUS
2019-04-29 15:50:33.939 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.access.vote.AffirmativeBased       : Voter: org.springframework.security.web.access.expression.WebExpressionVoter@3cd75674, returned: -1
2019-04-29 15:50:33.942 DEBUG 37625 --- [  XNIO-1 task-1] o.s.s.w.a.ExceptionTranslationFilter     : Access is denied (user is anonymous); redirecting to authentication entry point
```

### WebAsyncManagerIntegrationFilter
* WebSecurityConfigurerAdapter getHttp 메소드에서 등록

### SecurityContextPersistenceFilter
* SecurityContextRepository 에서 SecurityContext 를 로드하고 저장하는 일을 담당
* SecurityContext 는 사용자의 보호되고 인증된 세션 

### HeaderWriterFilter

### CsrfFilter

### LogoutFilter

### UsernamePasswordAuthenticationFilter

### DefaultLoginPageGeneratingFilter

### DefaultLogoutPageGeneratingFilter

### BasicAuthenticationFilter

### RequestCacheAwareFilter

### SecurityContextHolderAwareRequestFilter

### AnonymousAuthenticationFilter

### SessionManagementFilter

### ExceptionTranslationFilter
* 보호된 요청을 처리하는 동안 발생할 수 있는 기대한 예외의 기본 라우팅과 위임을 처리

### FilterSecurityInterceptor
* 권한부여와 관련한 결정을 AccessDecisionManager 에게 위임해 권한부여 결정 및 접근 제어 결정을 쉽게 만듬

### 참고
* Spring Security3 (피터 뮬라리엔)
* https://docs.spring.io/spring-security/site/docs/current/guides/html5/helloworld-boot.html