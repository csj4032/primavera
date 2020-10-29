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

## 설정 (SecurityConfig)

```java

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class PrimaveraSecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthenticationSuccessHandler successHandler = (request, response, authentication) -> log.info("success : " + request.getContextPath());
    private AuthenticationFailureHandler failureHandler = (request, response, authentication) -> log.info("failure : " + request.getContextPath());

    @Autowired
    private PrimaveraUserDetailsService primaveraUserDetailsService;

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser("Genius").password("{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.").roles("USER")
                .and()
                .withUser("Marcus Tullius Cicero").password(encoder.encode("password")).roles("MANAGER")
                .and()
                .withUser("Julius Caesar").password(encoder.encode("password")).roles("ADMINISTRATOR");
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        webSecurity.ignoring().antMatchers(HttpMethod.GET, "/resources/**", "/bower_components/**", "/dist/**", "/plugins/**", "/favicon.ico");
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterAfter(new PrimaveraFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin()
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/login")
                .loginProcessingUrl("/signin")
                .successHandler(successHandler)
                .defaultSuccessUrl("/index", true)
                .failureHandler(failureHandler)
                .failureUrl("/login?error=true")
                .and()
                .logout()
                .logoutUrl("/signout")
                .deleteCookies("JSESSIONID");
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(primaveraUserDetailsService);
        return authProvider;
    }
}
```

### DaoAuthenticationProvider
* additionalAuthenticationChecks 메소드에서 저장된 비밀번호와 로그인 화면에서 입력하 비밀번호를 확인

### Thymeleaf + Security
```html
<li class="dropdown user user-menu">
    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
        <img src="dist/img/user2-160x160.jpg" class="user-image" alt="User Image">
        <span sec:authentication="name" class="hidden-xs">Alexander Pierce</span>
    </a>
    <ul class="dropdown-menu">
        <!-- User image -->
        <li class="user-header">
            <img src="dist/img/user2-160x160.jpg" class="img-circle" alt="User Image">
            <p>
                <span sec:authentication="name">Alexander Pierce</span>
                <span sec:authorize="hasRole('ROLE_USER')">- Web Design</span>
                <span sec:authorize="hasRole('ROLE_MANAGER')">- Web Developer</span>
                <span sec:authorize="hasRole('ROLE_ADMINISTRATOR')">- Web Master</span>
                <small>Member since Nov. 2019</small>
            </p>
        </li>
        <!-- Menu Body -->
        <li class="user-body">
            <div class="row">
                <div class="col-xs-4 text-center">
                    <a href="#">Followers</a>
                </div>
                <div class="col-xs-4 text-center">
                    <a href="#">Sales</a>
                </div>
                <div class="col-xs-4 text-center">
                    <a href="#">Friends</a>
                </div>
            </div>
            <!-- /.row -->
        </li>
        <!-- Menu Footer-->
        <li class="user-footer">
            <div class="pull-left">
                <a href="#" class="btn btn-default btn-flat">Profile</a>
            </div>
            <div class="pull-right">
                <a th:href="@{/signout}" href="/signout" class="btn btn-default btn-flat">Sign out</a>
            </div>
        </li>
    </ul>
</li>
```

### PasswordEncoding Test
```java
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PasswordEncoderTest {

    private static PasswordEncoder encoder;
    private static String rawPassword = "password";
    private static String bcrype = "{bcrypt}$2a$10$7UEHLpn1r4gZY2qxiZFJ5.7wa3Hdz8IXgxUtFogy0Ac10fh7TG4V.";
    private static String noop = "{noop}password";

    @BeforeAll
    public static void setUp() {
        encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Test
    @Order(1)
    @DisplayName("bcrype 방식")
    public void bcrypeEncoder() {
        String encodedPassword = encoder.encode(rawPassword);
        log.info(encodedPassword);
        Assertions.assertNotEquals(bcrype, encodedPassword);
        Assertions.assertTrue(encoder.matches(rawPassword, encodedPassword));
        Assertions.assertTrue(encoder.matches(rawPassword, bcrype));
    }

    @Test
    @Order(2)
    @DisplayName("noop 방식")
    public void noopEncoder() {
        Assertions.assertTrue(encoder.matches(rawPassword, noop));
    }
}
```

### Spring-Boot-Starter

### 참고
* Spring Security3 (피터 뮬라리엔)
* https://docs.spring.io/spring-security/site/docs/current/guides/html5/helloworld-boot.html