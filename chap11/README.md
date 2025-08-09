# Chapter 11: OAuth2 기반 게시판 시스템과 고급 보안 설정

## 프로젝트 개요

**Board System with OAuth2**는 소셜 로그인을 활용한 완전한 게시판 시스템입니다. OAuth2 인증을 통해 사용자 관리를 하고, WYSIWYG 에디터를 포함한 게시글 작성, 페이징, 검색 기능을 구현합니다. Spring Security의 고급 보안 설정과 Thymeleaf 템플릿 엔진을 활용한 동적 UI를 학습할 수 있습니다.

### 보안 학습 목표
- OAuth2 기반 인증과 일반 로그인의 하이브리드 구성
- 게시글 권한 기반 접근 제어 (RBAC)
- CSRF 보호와 XSS 방어를 포함한 보안 강화
- 메서드 레벨 보안을 통한 세밀한 권한 제어
- 동적 권한 검사 및 컨텐츠 필터링

## 프로젝트 구조

```
chap11/
├── src/main/java/com/genius/primavera/
│   ├── BoardSystemApplication.java                 # 메인 애플리케이션
│   ├── domain/
│   │   ├── model/
│   │   │   ├── user/                              # 사용자 도메인
│   │   │   │   ├── User.java                      # 사용자 엔티티
│   │   │   │   ├── UserConnection.java            # 소셜 연동 정보
│   │   │   │   ├── Role.java                      # 역할 엔티티
│   │   │   │   ├── UserRole.java                  # 사용자-역할 매핑
│   │   │   │   ├── RoleType.java                  # 역할 타입
│   │   │   │   ├── ProviderType.java              # 소셜 프로바이더 타입
│   │   │   │   └── UserStatus.java                # 사용자 상태
│   │   │   └── post/                              # 게시글 도메인
│   │   │       ├── Post.java                      # 게시글 엔티티
│   │   │       ├── PostDto.java                   # 게시글 DTO
│   │   │       ├── PostStatus.java                # 게시글 상태
│   │   │       └── PostStatusConverter.java       # 상태 변환기
│   │   ├── mapper/
│   │   │   ├── UserMapper.java                    # 사용자 매퍼
│   │   │   ├── UserRoleMapper.java                # 역할 매퍼
│   │   │   ├── UserConnectionMapper.java          # 소셜 연동 매퍼
│   │   │   └── PostMapper.java                    # 게시글 매퍼
│   │   ├── PageRequest.java                       # 페이징 요청
│   │   └── Paged.java                             # 페이징 결과
│   ├── application/
│   │   ├── user/
│   │   │   ├── UserService.java                   # 사용자 서비스
│   │   │   └── UserServiceImpl.java               # 서비스 구현
│   │   └── post/
│   │       ├── PostingService.java                # 게시글 서비스
│   │       └── PostingServiceImpl.java            # 서비스 구현
│   ├── infrastructure/
│   │   ├── configuration/
│   │   │   └── PrimaveraProperties.java           # 애플리케이션 속성
│   │   ├── security/
│   │   │   ├── PrimaveraSecurityConfiguration.java # Spring Security 설정
│   │   │   ├── PrimaveraUserDetailsService.java   # 사용자 인증 서비스
│   │   │   ├── PrimaveraSocialUserDetailsService.java # 소셜 인증 서비스
│   │   │   ├── PrimaveraUserDetails.java          # 커스텀 UserDetails
│   │   │   ├── PrimaveraAuthenticationSuccessHandler.java # 인증 성공 핸들러
│   │   │   └── social/                            # 소셜 인증 설정
│   │   │       ├── PrimaveraSocialConfiguration.java
│   │   │       └── provider/                      # 프로바이더별 설정
│   │   │           ├── GoogleUserDetails.java
│   │   │           ├── FacebookUserDetails.java
│   │   │           └── GithubUserDetails.java
│   │   └── filter/
│   │       └── PrimaveraFilter.java              # 커스텀 보안 필터
│   └── interfaces/
│       ├── LoginController.java                   # 로그인 컨트롤러
│       ├── UserController.java                    # 사용자 컨트롤러
│       └── PostingController.java                 # 게시글 컨트롤러
├── src/main/resources/
│   ├── application.yml                            # 메인 설정
│   ├── application-local.yml                     # 로컬 개발 설정
│   └── templates/                                 # Thymeleaf 템플릿
│       ├── login.html                            # 로그인 페이지
│       ├── index.html                            # 메인 페이지
│       ├── admin.html                            # 관리자 페이지
│       └── manager.html                          # 매니저 페이지
└── src/test/resources/
    ├── application-test.yml                      # 테스트 설정
    └── sql/init.sql                             # 테스트 데이터
```

## 보안 기능 및 OAuth2 + 일반 로그인 하이브리드

### 1. 하이브리드 인증 설정

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class PrimaveraSecurityConfiguration {
    
    private final PrimaveraUserDetailsService userDetailsService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMINISTRATOR")
                .requestMatchers("/manager/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                .requestMatchers("/posts/write", "/posts/edit/**", "/posts/delete/**")
                    .hasAnyRole("USER", "MANAGER", "ADMINISTRATOR")
                .requestMatchers("/", "/login/**", "/oauth2/**", "/posts/view/**").permitAll()
                .anyRequest().authenticated()
            )
            // 일반 폼 로그인 설정
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/signin")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
            )
            // OAuth2 소셜 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .userInfoEndpoint(userInfo -> 
                    userInfo.userService(oauth2UserService)
                )
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")
            )
            .build();
    }
}
```

### 2. 게시글 권한 기반 접근 제어

```java
@Service
@Transactional(readOnly = true)
public class PostingServiceImpl implements PostingService {
    
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMINISTRATOR')")
    @Transactional
    public Post createPost(PostDto postDto, Authentication authentication) {
        validateUserPermission(authentication);
        
        Post post = Post.builder()
            .title(postDto.getTitle())
            .content(postDto.getContent())
            .author(getCurrentUser(authentication))
            .status(PostStatus.PUBLISHED)
            .build();
            
        return postMapper.save(post);
    }
    
    @PreAuthorize("@postingServiceImpl.isPostOwner(#postId, authentication) or hasRole('ADMINISTRATOR')")
    @Transactional
    public Post updatePost(Long postId, PostDto postDto, Authentication authentication) {
        Post existingPost = postMapper.findById(postId)
            .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다: " + postId));
            
        existingPost.updateContent(postDto.getTitle(), postDto.getContent());
        return postMapper.update(existingPost);
    }
    
    @PreAuthorize("@postingServiceImpl.isPostOwner(#postId, authentication) or hasRole('ADMINISTRATOR')")
    @Transactional
    public void deletePost(Long postId, Authentication authentication) {
        postMapper.deleteById(postId);
    }
    
    public boolean isPostOwner(Long postId, Authentication authentication) {
        return postMapper.findById(postId)
            .map(post -> post.getAuthor().getEmail().equals(authentication.getName()))
            .orElse(false);
    }
}
```

### 3. 페이징 및 검색 기능

```java
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostingController {
    
    private final PostingService postingService;
    
    @GetMapping
    public String listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Model model) {
        
        PageRequest pageRequest = PageRequest.builder()
            .page(page)
            .size(size)
            .keyword(keyword)
            .category(category)
            .build();
            
        Paged<Post> pagedPosts = postingService.findPosts(pageRequest);
        
        model.addAttribute("posts", pagedPosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedPosts.getTotalPages());
        model.addAttribute("keyword", keyword);
        
        return "posts/list";
    }
    
    @GetMapping("/write")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMINISTRATOR')")
    public String writePostForm(Model model, Authentication authentication) {
        model.addAttribute("postDto", new PostDto());
        model.addAttribute("user", authentication.getPrincipal());
        return "posts/write";
    }
    
    @PostMapping("/write")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMINISTRATOR')")
    public String writePost(@Valid @ModelAttribute PostDto postDto, 
                           BindingResult bindingResult,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "posts/write";
        }
        
        try {
            Post savedPost = postingService.createPost(postDto, authentication);
            redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 작성되었습니다.");
            return "redirect:/posts/view/" + savedPost.getId();
        } catch (Exception e) {
            bindingResult.reject("post.save.error", "게시글 저장 중 오류가 발생했습니다.");
            return "posts/write";
        }
    }
}
```

## 기술 스택

- **Spring Boot**: 3.3.6
- **Spring Security**: 6.4.4
- **OAuth2 Client**: 소셜 로그인 통합
- **Spring Web**: MVC 패턴 구현
- **Spring Validation**: 입력 데이터 검증
- **MyBatis**: SQL 매핑 프레임워크
- **Thymeleaf**: 템플릿 엔진
- **Thymeleaf Security**: 보안 확장 기능
- **MariaDB**: 관계형 데이터베이스
- **TestContainers**: 통합 테스트 컨테이너
- **WYSIWYG Editor**: 리치 텍스트 에디터

## 실행 방법

### 1. 환경 변수 설정
```bash
# OAuth2 클라이언트 정보 설정 (선택사항)
export OAUTH2_GOOGLE_CLIENTID=your-google-client-id
export OAUTH2_GOOGLE_CLIENTSECRET=your-google-client-secret
export OAUTH2_GITHUB_CLIENTID=your-github-client-id
export OAUTH2_GITHUB_CLIENTSECRET=your-github-client-secret
```

### 2. Docker 인프라 시작
```bash
# MariaDB 시작
./docker-manager.sh start chap11

# 상태 확인
./docker-manager.sh status chap11
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap11:bootRun -Dspring.profiles.active=local

# 또는 IDE에서 실행 시
-Dspring.profiles.active=local
```

### 4. 웹 접속
```
http://localhost:8080
```

### 5. 테스트 계정
- **관리자**: admin@primavera.com / password (ADMINISTRATOR 권한)
- **매니저**: manager@primavera.com / password (MANAGER 권한)
- **사용자**: user@primavera.com / password (USER 권한)
- **소셜 로그인**: Google, GitHub 계정 사용 가능

## 보안 테스트 실행 방법

### 1. 전체 테스트
```bash
./gradlew :chap11:test
```

### 2. OAuth2 인증 테스트
```bash
./gradlew :chap11:test --tests "*OAuth2*"
```

### 3. 게시글 권한 테스트
```bash
./gradlew :chap11:test --tests "*Post*"
```

### 4. 통합 테스트
```bash
./gradlew :chap11:test --tests "*Integration*"
```

## 핵심 보안 학습 포인트

### 1. 동적 권한 검사

```java
@Component
public class PostPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof Post post) {
            return evaluatePostPermission(authentication, post, permission.toString());
        }
        return false;
    }
    
    private boolean evaluatePostPermission(Authentication authentication, Post post, String permission) {
        return switch (permission) {
            case "read" -> post.getStatus() == PostStatus.PUBLISHED || isAuthorOrAdmin(authentication, post);
            case "write", "edit" -> isAuthorOrAdmin(authentication, post);
            case "delete" -> isAuthorOrAdmin(authentication, post) || hasRole(authentication, "ADMINISTRATOR");
            default -> false;
        };
    }
    
    private boolean isAuthorOrAdmin(Authentication authentication, Post post) {
        String currentUser = authentication.getName();
        return post.getAuthor().getEmail().equals(currentUser) || 
               hasRole(authentication, "ADMINISTRATOR");
    }
}
```

### 2. CSRF 보호 및 XSS 방어

```html
<!-- Thymeleaf 템플릿에서 CSRF 토큰 사용 -->
<form th:action="@{/posts/write}" method="post" th:object="${postDto}">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    
    <div class="form-group">
        <label for="title">제목</label>
        <input type="text" 
               id="title" 
               th:field="*{title}" 
               class="form-control"
               th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : ''"
               required>
        <div th:if="${#fields.hasErrors('title')}" class="invalid-feedback">
            <span th:errors="*{title}"></span>
        </div>
    </div>
    
    <!-- XSS 방어를 위한 텍스트 이스케이핑 -->
    <div class="form-group">
        <label for="content">내용</label>
        <textarea id="content" 
                  th:field="*{content}" 
                  class="form-control wysiwyg-editor"
                  th:classappend="${#fields.hasErrors('content')} ? 'is-invalid' : ''"
                  rows="10" required></textarea>
    </div>
    
    <button type="submit" class="btn btn-primary">게시글 작성</button>
</form>
```

### 3. 세션 고정 공격 방어

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
            .sessionRegistry(sessionRegistry())
            .and()
            .sessionFixation().changeSessionId() // 세션 고정 공격 방어
            .invalidSessionUrl("/login?expired=true")
        )
        .build();
}
```

### 4. Thymeleaf 보안 확장

```html
<!-- 인증된 사용자만 표시 -->
<div sec:authorize="isAuthenticated()">
    <p>안녕하세요, <span sec:authentication="name"></span>님!</p>
    
    <!-- 권한별 메뉴 표시 -->
    <ul class="nav">
        <li sec:authorize="hasRole('USER')">
            <a th:href="@{/posts/write}">글쓰기</a>
        </li>
        <li sec:authorize="hasRole('MANAGER')">
            <a th:href="@{/manager/dashboard}">관리 대시보드</a>
        </li>
        <li sec:authorize="hasRole('ADMINISTRATOR')">
            <a th:href="@{/admin/users}">사용자 관리</a>
        </li>
    </ul>
</div>

<!-- 게시글 작성자만 수정/삭제 버튼 표시 -->
<div th:if="${post.author.email == #authentication.name or #authorization.expression('hasRole(''ADMINISTRATOR'')')}">
    <a th:href="@{/posts/edit/{id}(id=${post.id})}" class="btn btn-warning">수정</a>
    <a th:href="@{/posts/delete/{id}(id=${post.id})}" 
       class="btn btn-danger" 
       onclick="return confirm('정말 삭제하시겠습니까?')">삭제</a>
</div>
```

## 학습 순서

1. **하이브리드 인증 구조 이해**
   - 일반 로그인과 소셜 로그인 통합
   - 다중 인증 제공자 설정
   - 인증 성공/실패 핸들러

2. **게시판 도메인 모델링**
   - Post, User, Role 엔티티 관계
   - 상태 관리 및 변환
   - DTO와 엔티티 매핑

3. **권한 기반 접근 제어**
   - URL 레벨 보안 설정
   - 메서드 레벨 보안
   - 동적 권한 검사

4. **페이징 및 검색 구현**
   - PageRequest와 Paged 활용
   - MyBatis 동적 쿼리
   - 검색 조건 처리

5. **보안 강화 설정**
   - CSRF 보호 설정
   - XSS 방어 구현
   - 세션 보안 설정

6. **Thymeleaf 고급 활용**
   - 보안 확장 기능
   - 조건부 렌더링
   - WYSIWYG 에디터 통합

## 주요 보안 애너테이션

### 권한 검사 애너테이션
- `@PreAuthorize`: 메서드 실행 전 권한 검사
- `@PostAuthorize`: 메서드 실행 후 권한 검사
- `@Secured`: 역할 기반 권한 검사
- `@RolesAllowed`: JSR-250 권한 검사

### Thymeleaf 보안 속성
- `sec:authorize`: 권한 기반 요소 표시
- `sec:authentication`: 인증 정보 접근
- `sec:authorize-url`: URL 접근 권한 검사

### 검증 애너테이션
- `@Valid`: 객체 유효성 검증
- `@NotBlank`: 빈 문자열 검사
- `@Size`: 길이 제한 검사
- `@Pattern`: 정규식 패턴 검사

## 다음 단계 안내

Chapter 11을 완료한 후에는 **Chapter 12 (계층형 댓글 시스템)**으로 진행하여 다음 내용을 학습합니다:

- 계층형 댓글 구조 설계
- 파일 업로드 및 다운로드 기능
- ModelMapper를 활용한 객체 매핑
- 고급 보안 설정 및 인터셉터
- 복합 도메인 모델 관리

---

OAuth2와 일반 로그인의 하이브리드 구성으로 실전 게시판 시스템을 완성해보세요!