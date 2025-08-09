# Chapter 12: 계층형 댓글 시스템과 파일 업로드 보안

## 프로젝트 개요

**Hierarchical Comment System**은 계층형 댓글 구조와 파일 업로드 기능을 갖춘 완전한 게시판 시스템입니다. OAuth2 소셜 로그인을 기반으로 하며, 트리 구조의 댓글 시스템, 파일 업로드/다운로드, ModelMapper를 통한 객체 매핑, 고급 보안 인터셉터를 구현합니다.

### 보안 학습 목표
- 파일 업로드 보안 및 검증 구현
- 계층형 데이터 구조에서의 권한 관리
- 파일 다운로드 시 보안 헤더 설정
- 입력 데이터 검증 및 XSS 방어 강화
- MyBatis 인터셉터를 통한 감사 로그

## 프로젝트 구조

```
chap12/
├── src/main/java/com/genius/primavera/
│   ├── HierarchicalCommentApplication.java         # 메인 애플리케이션
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
│   │   │   ├── post/                              # 게시글 도메인
│   │   │   │   ├── Post.java                      # 게시글 엔티티
│   │   │   │   ├── PostDto.java                   # 게시글 DTO
│   │   │   │   └── PostStatus.java                # 게시글 상태
│   │   │   ├── article/                           # 게시글 확장
│   │   │   │   ├── Article.java                   # 게시글 엔티티
│   │   │   │   ├── ArticleDto.java                # 게시글 DTO
│   │   │   │   ├── Content.java                   # 게시글 내용
│   │   │   │   ├── Comment.java                   # 댓글 엔티티
│   │   │   │   ├── CommentDto.java                # 댓글 DTO
│   │   │   │   ├── Reply.java                     # 답글 엔티티
│   │   │   │   ├── ArticleStatus.java             # 게시글 상태
│   │   │   │   ├── WriteType.java                 # 작성 타입
│   │   │   │   └── WriteTypeConverter.java        # 타입 변환기
│   │   │   └── BannerLink.java                    # 배너 링크
│   │   ├── mapper/
│   │   │   ├── UserMapper.java                    # 사용자 매퍼
│   │   │   ├── UserRoleMapper.java                # 역할 매퍼
│   │   │   ├── UserConnectionMapper.java          # 소셜 연동 매퍼
│   │   │   ├── PostMapper.java                    # 게시글 매퍼
│   │   │   └── article/                           # 게시글 매퍼 확장
│   │   │       ├── ArticleMapper.java             # 게시글 매퍼
│   │   │       ├── ArticleContentMapper.java      # 내용 매퍼
│   │   │       └── ArticleCommentMapper.java      # 댓글 매퍼
│   │   ├── PageRequest.java                       # 페이징 요청
│   │   ├── Paged.java                             # 페이징 결과
│   │   └── ArticleNotFoundException.java          # 게시글 예외
│   ├── application/
│   │   ├── user/
│   │   │   ├── UserService.java                   # 사용자 서비스
│   │   │   └── UserServiceImpl.java               # 서비스 구현
│   │   ├── post/
│   │   │   ├── PostingService.java                # 게시글 서비스
│   │   │   └── PostingServiceImpl.java            # 서비스 구현
│   │   ├── article/
│   │   │   ├── WriteArticleService.java           # 게시글 작성 서비스
│   │   │   └── WriteArticleServiceImpl.java       # 서비스 구현
│   │   └── PrimaveraUtil.java                     # 유틸리티 클래스
│   ├── infrastructure/
│   │   ├── configuration/
│   │   │   └── PrimaveraProperties.java           # 애플리케이션 속성
│   │   ├── security/
│   │   │   ├── PrimaveraSecurityConfiguration.java # Spring Security 설정
│   │   │   ├── PrimaveraUserDetailsService.java   # 사용자 인증 서비스
│   │   │   ├── PrimaveraSocialUserDetailsService.java # 소셜 인증 서비스
│   │   │   ├── PrimaveraUserDetails.java          # 커스텀 UserDetails
│   │   │   ├── PrimaveraAuthenticationSuccessHandler.java # 인증 성공 핸들러
│   │   │   └── ClientResources.java               # OAuth2 클라이언트 리소스
│   │   ├── intercepts/
│   │   │   └── PrimaveraIntercepts.java          # MyBatis 인터셉터
│   │   └── filter/
│   │       └── PrimaveraFilter.java              # 커스텀 보안 필터
│   └── interfaces/
│       ├── LoginController.java                   # 로그인 컨트롤러
│       ├── UserController.java                    # 사용자 컨트롤러
│       ├── PostingController.java                 # 게시글 컨트롤러
│       ├── ArticleController.java                 # 게시글 확장 컨트롤러
│       └── FilterController.java                  # 필터 테스트 컨트롤러
├── src/main/resources/
│   ├── application.yml                            # 메인 설정
│   ├── application-default.yml                   # 기본 설정
│   ├── application-local.yml                     # 로컬 개발 설정
│   ├── social.yml                                 # 소셜 로그인 설정
│   ├── db/                                       # 데이터베이스 초기화
│   └── templates/                                 # Thymeleaf 템플릿
│       ├── login.html                            # 로그인 페이지
│       ├── index.html                            # 메인 페이지
│       ├── admin.html                            # 관리자 페이지
│       └── manager.html                          # 매니저 페이지
└── src/test/resources/
    ├── application-test.yml                      # 테스트 설정
    ├── social.yml                                # 소셜 로그인 테스트 설정
    └── sql/init.sql                             # 테스트 데이터
```

## 보안 기능 및 파일 업로드 보안

### 1. 파일 업로드 보안 설정

```java
@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
        "image/jpeg", "image/png", "image/gif", 
        "application/pdf", "text/plain"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMINISTRATOR')")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        try {
            validateFile(file);
            String fileName = secureFileName(file.getOriginalFilename());
            String filePath = saveFileSecurely(file, fileName, authentication);
            
            return ResponseEntity.ok(Map.of(
                "fileName", fileName,
                "filePath", filePath,
                "size", file.getSize()
            ));
        } catch (FileUploadException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    private void validateFile(MultipartFile file) throws FileUploadException {
        if (file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("파일 크기가 너무 큽니다. (최대 10MB)");
        }
        
        String contentType = file.getContentType();
        if (!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileUploadException("허용되지 않는 파일 형식입니다: " + contentType);
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && containsMaliciousExtension(originalFilename)) {
            throw new FileUploadException("위험한 파일 확장자가 감지되었습니다.");
        }
    }
    
    private String secureFileName(String originalFilename) {
        if (originalFilename == null) {
            return "file_" + System.currentTimeMillis();
        }
        
        // 파일명 정규화 및 보안 처리
        String sanitized = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return timestamp + "_" + sanitized;
    }
}
```

### 2. 계층형 댓글 시스템

```java
@Mapper
@Repository
public interface ArticleCommentMapper {
    
    // 계층형 댓글 조회 (인접 목록 모델)
    @Results({
        @Result(property = "id", column = "ID"),
        @Result(property = "articleId", column = "ARTICLE_ID"),
        @Result(property = "level", column = "LEVEL"),
        @Result(property = "step", column = "STEP"),
        @Result(property = "comment", column = "COMMENT"),
        @Result(property = "author", column = "AUTHOR"),
        @Result(property = "status", column = "STATUS"),
        @Result(property = "children", javaType = List.class, column = "ID", 
                many = @Many(select = "findChildComments", fetchType = FetchType.LAZY))
    })
    @Select("""
        SELECT ID, ARTICLE_ID, LEVEL, STEP, COMMENT, AUTHOR, STATUS, REG_DT, MOD_DT 
        FROM ARTICLE_COMMENT 
        WHERE ARTICLE_ID = #{articleId} AND LEVEL = 0 
        ORDER BY STEP ASC
    """)
    List<Comment> findRootComments(Long articleId);
    
    @Select("""
        SELECT ID, ARTICLE_ID, LEVEL, STEP, COMMENT, AUTHOR, STATUS, REG_DT, MOD_DT 
        FROM ARTICLE_COMMENT 
        WHERE ARTICLE_ID = (SELECT ARTICLE_ID FROM ARTICLE_COMMENT WHERE ID = #{parentId}) 
        AND LEVEL = (SELECT LEVEL + 1 FROM ARTICLE_COMMENT WHERE ID = #{parentId})
        AND STEP > (SELECT STEP FROM ARTICLE_COMMENT WHERE ID = #{parentId})
        ORDER BY STEP ASC
    """)
    List<Comment> findChildComments(Long parentId);
    
    // 댓글 작성 시 계층 구조 관리
    @Insert("""
        INSERT INTO ARTICLE_COMMENT (ARTICLE_ID, LEVEL, STEP, COMMENT, AUTHOR, STATUS)
        VALUES (#{articleId}, #{level}, #{step}, #{comment}, #{author}, #{status})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(Comment comment);
    
    // 답글 작성 시 STEP 업데이트
    @Update("""
        UPDATE ARTICLE_COMMENT 
        SET STEP = STEP + 1 
        WHERE ARTICLE_ID = #{articleId} AND STEP > #{parentStep}
    """)
    int updateStepForReply(@Param("articleId") Long articleId, @Param("parentStep") int parentStep);
}
```

### 3. 파일 다운로드 보안

```java
@GetMapping("/download/{fileId}")
@PreAuthorize("@articleController.canDownloadFile(#fileId, authentication)")
public ResponseEntity<Resource> downloadFile(
        @PathVariable Long fileId,
        Authentication authentication,
        HttpServletRequest request) {
    
    try {
        FileInfo fileInfo = articleService.getFileInfo(fileId);
        Resource resource = articleService.loadFileAsResource(fileInfo.getFilePath());
        
        // 파일 접근 권한 검사
        if (!hasFileAccessPermission(fileInfo, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String contentType = getContentType(request, resource);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + fileInfo.getOriginalName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
                
    } catch (FileNotFoundException e) {
        return ResponseEntity.notFound().build();
    } catch (SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}

public boolean canDownloadFile(Long fileId, Authentication authentication) {
    return articleService.getFileInfo(fileId)
        .map(fileInfo -> hasFileAccessPermission(fileInfo, authentication))
        .orElse(false);
}

private boolean hasFileAccessPermission(FileInfo fileInfo, Authentication authentication) {
    // 파일 소유자이거나 관리자인 경우 접근 허용
    String currentUser = authentication.getName();
    return fileInfo.getUploader().equals(currentUser) || 
           hasRole(authentication, "ADMINISTRATOR");
}
```

### 4. ModelMapper를 통한 객체 매핑 보안

```java
@Configuration
public class ModelMapperConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
            
        // 보안 필드 제외 설정
        configureSecurityMappings(mapper);
        
        return mapper;
    }
    
    private void configureSecurityMappings(ModelMapper mapper) {
        // User 엔티티 매핑 시 민감한 필드 제외
        mapper.typeMap(User.class, UserDto.class)
            .addMappings(mapping -> {
                mapping.skip(UserDto::setPassword);
                mapping.skip(UserDto::setSalt);
                mapping.skip(UserDto::setSecretKey);
            });
            
        // 댓글 매핑 시 작성자 개인정보 보호
        mapper.typeMap(Comment.class, CommentDto.class)
            .addMappings(mapping -> {
                mapping.map(src -> maskEmail(src.getAuthor().getEmail()), 
                          CommentDto::setAuthorEmail);
            });
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "***@" + domain;
        }
        
        return username.substring(0, 2) + "***@" + domain;
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
- **ModelMapper**: 객체 매핑 라이브러리
- **Thymeleaf**: 템플릿 엔진
- **Commons IO**: 파일 처리 유틸리티
- **Commons Codec**: 인코딩 유틸리티
- **MariaDB**: 관계형 데이터베이스
- **TestContainers**: 통합 테스트 컨테이너
- **Lucy XSS Filter**: XSS 방어

## 실행 방법

### 1. 환경 변수 설정
```bash
# OAuth2 클라이언트 정보 설정 (social.yml에서 관리)
# 파일 업로드 경로 설정
export PRIMAVERA_UPLOAD_PATH=/path/to/upload/directory
```

### 2. Docker 인프라 시작
```bash
# MariaDB 시작
./docker-manager.sh start chap12

# 상태 확인
./docker-manager.sh status chap12
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap12:bootRun -Dspring.profiles.active=local

# 또는 IDE에서 실행 시
-Dspring.profiles.active=local
```

### 4. 웹 접속
```
http://localhost:8080
```

## 보안 테스트 실행 방법

### 1. 전체 테스트
```bash
./gradlew :chap12:test
```

### 2. 파일 업로드 보안 테스트
```bash
./gradlew :chap12:test --tests "*Upload*"
```

### 3. 댓글 시스템 테스트
```bash
./gradlew :chap12:test --tests "*Comment*"
```

### 4. 통합 테스트
```bash
./gradlew :chap12:test --tests "*Integration*"
```

## 핵심 보안 학습 포인트

### 1. 파일 업로드 취약점 방어

```java
@Component
public class FileSecurityValidator {
    
    private static final List<String> DANGEROUS_EXTENSIONS = List.of(
        ".jsp", ".php", ".exe", ".sh", ".bat", ".cmd", ".scr", ".vbs"
    );
    
    private static final Map<String, List<String>> MIME_TYPE_EXTENSIONS = Map.of(
        "image/jpeg", List.of(".jpg", ".jpeg"),
        "image/png", List.of(".png"),
        "image/gif", List.of(".gif"),
        "application/pdf", List.of(".pdf"),
        "text/plain", List.of(".txt")
    );
    
    public void validateFileUpload(MultipartFile file) throws FileUploadException {
        validateFileSize(file);
        validateMimeType(file);
        validateFileName(file.getOriginalFilename());
        validateFileContent(file);
    }
    
    private void validateFileContent(MultipartFile file) throws FileUploadException {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[512];
            int bytesRead = inputStream.read(header);
            
            // 파일 시그니처 검증
            if (!isValidFileSignature(header, file.getContentType())) {
                throw new FileUploadException("파일 내용이 확장자와 일치하지 않습니다.");
            }
            
            // 악성 스크립트 패턴 검사
            String content = new String(header, StandardCharsets.UTF_8);
            if (containsMaliciousScript(content)) {
                throw new FileUploadException("악성 스크립트가 감지되었습니다.");
            }
            
        } catch (IOException e) {
            throw new FileUploadException("파일 내용을 검증할 수 없습니다.");
        }
    }
}
```

### 2. 계층형 데이터 보안

```java
@Service
@Transactional(readOnly = true)
public class CommentSecurityService {
    
    @PreAuthorize("hasPermission(#comment, 'read')")
    public Comment getComment(Long commentId, Authentication authentication) {
        Comment comment = commentMapper.findById(commentId)
            .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));
            
        // 삭제된 댓글의 경우 내용 마스킹
        if (comment.getStatus() == CommentStatus.DELETED) {
            return maskDeletedComment(comment);
        }
        
        return comment;
    }
    
    @PreAuthorize("hasRole('USER') and @commentSecurityService.canReplyToComment(#parentCommentId, authentication)")
    @Transactional
    public Comment createReply(Long parentCommentId, CommentDto replyDto, Authentication authentication) {
        Comment parentComment = getComment(parentCommentId, authentication);
        
        // 계층 깊이 제한 (최대 5단계)
        if (parentComment.getLevel() >= 4) {
            throw new CommentException("더 이상 답글을 달 수 없습니다.");
        }
        
        // STEP 값 조정 (기존 답글들의 순서 재정렬)
        commentMapper.updateStepForReply(parentComment.getArticleId(), parentComment.getStep());
        
        Comment reply = Comment.builder()
            .articleId(parentComment.getArticleId())
            .level(parentComment.getLevel() + 1)
            .step(parentComment.getStep() + 1)
            .comment(replyDto.getComment())
            .author(getCurrentUser(authentication))
            .status(CommentStatus.ACTIVE)
            .build();
            
        return commentMapper.save(reply);
    }
    
    public boolean canReplyToComment(Long commentId, Authentication authentication) {
        return commentMapper.findById(commentId)
            .map(comment -> comment.getStatus() == CommentStatus.ACTIVE)
            .orElse(false);
    }
}
```

### 3. MyBatis 보안 인터셉터

```java
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class PrimaveraIntercepts implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        
        // SQL 인젝션 패턴 검사
        if (parameter != null) {
            validateSqlInjection(parameter);
        }
        
        // 감사 로그 기록
        recordAuditLog(statement, parameter);
        
        try {
            Object result = invocation.proceed();
            
            // 결과 민감 정보 마스킹
            return maskSensitiveData(result);
            
        } catch (Exception e) {
            recordErrorLog(statement, parameter, e);
            throw e;
        }
    }
    
    private void validateSqlInjection(Object parameter) throws SQLException {
        String paramStr = parameter.toString().toLowerCase();
        
        List<String> dangerousPatterns = List.of(
            "union", "select", "insert", "update", "delete", "drop", 
            "exec", "script", "javascript", "vbscript"
        );
        
        for (String pattern : dangerousPatterns) {
            if (paramStr.contains(pattern)) {
                throw new SQLException("잠재적 SQL 인젝션이 감지되었습니다: " + pattern);
            }
        }
    }
}
```

## 학습 순서

1. **계층형 데이터 구조 이해**
   - 인접 목록 모델 vs 중첩 집합 모델
   - 트리 구조 탐색 및 정렬
   - STEP과 LEVEL을 통한 계층 관리

2. **파일 업로드 보안**
   - 파일 타입 및 크기 검증
   - 파일명 정규화 및 보안 처리
   - 파일 시그니처 검증

3. **파일 다운로드 보안**
   - 접근 권한 검사
   - 보안 헤더 설정
   - 파일 경로 조작 방지

4. **객체 매핑 보안**
   - ModelMapper 설정
   - 민감한 필드 제외
   - 타입 안전성 보장

5. **데이터베이스 보안**
   - MyBatis 인터셉터 구현
   - SQL 인젝션 방지
   - 감사 로그 기록

6. **고급 인증/인가**
   - 동적 권한 검사
   - 리소스 기반 접근 제어
   - 메서드 레벨 보안

## 주요 보안 애너테이션

### 파일 처리 관련
- `@Valid`: 파일 업로드 객체 검증
- `@RequestParam`: 멀티파트 파일 바인딩
- `@PreAuthorize`: 파일 접근 권한 검사

### 데이터베이스 관련
- `@Transactional`: 트랜잭션 경계
- `@Intercepts`: MyBatis 인터셉터
- `@Results`: 결과 매핑 및 보안

### 객체 매핑 관련
- `@Mapper`: ModelMapper 자동 매핑
- `@Mapping`: 커스텀 필드 매핑
- `@JsonIgnore`: JSON 직렬화 제외

## 다음 단계 안내

Chapter 12를 완료한 후에는 **Chapter 13 이상의 고급 주제들**로 진행하여 다음 내용을 학습합니다:

- 마이크로서비스 아키텍처
- 분산 시스템 보안
- API Gateway와 서비스 메시
- 컨테이너 보안
- 클라우드 네이티브 보안

---

계층형 댓글 시스템과 파일 업로드 보안을 마스터하여 완전한 웹 애플리케이션 개발 역량을 완성해보세요!