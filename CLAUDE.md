# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Primavera - Spring Boot Educational Project

## Quick Development Commands

### Build and Test
```bash
# Build entire project
./gradlew clean build

# Build specific module
./gradlew :chap09:build

# Run tests for specific module
./gradlew :chap11:test

# Run specific test class
./gradlew :chap11:test --tests MariaDBContainerTest

# Run application (specific chapter)
./gradlew :chap09:bootRun

# Build with parallel execution
./gradlew build --parallel
```

### Database Operations
```bash
# Start MariaDB 11.4.7 with Docker
docker run -d --name mariadb-primavera \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3306:3306 mariadb:11.4.7

# Use docker-compose for complete setup (recommended)
docker-compose up -d

# TestContainers automatically manages MariaDB 11.4.7 for tests
```

### Version Management
All dependency versions are centralized in `gradle.properties`. When adding new dependencies:
1. Add version to appropriate category in `gradle.properties` (alphabetically ordered)
2. Reference using `${versionVariableName}` in build.gradle files
3. Never hardcode versions in build.gradle files

## Project Architecture

### Multi-Module Educational Structure
This is a **progressive learning project** with 18 modules (chap00-chap17 + utilities) that demonstrate Spring Boot concepts from basic to advanced. Each module is **intentionally independent** with its own complete implementation.

**Key Architectural Principle**: Educational duplication is intentionally allowed over DRY principles to demonstrate concept evolution across modules.

### Module Categories
- **chap00-04**: Core Spring Boot fundamentals
- **chap05-08**: Web development, security, and data access
- **chap09-13**: Advanced features (OAuth2, complex data structures)
- **chap14-17**: Production concerns (reactive, microservices, monitoring)
- **Utilities**: Custom Spring Boot starters

### Database Strategy
- **Production**: MariaDB 11.4.7 (standardized across all modules)
- **Testing**: TestContainers with MariaDB 11.4.7 (ensures environment consistency)
- **Schema Management**: Flyway migrations + JPA DDL auto-generation
- **Audit Strategy**: JPA Auditing + Hibernate Envers for all entities

## Development Philosophy

### Code Quality Standards
- **TDD First**: Write tests before implementation
- **Functional Programming**: Prefer Stream API, Optional, immutable objects
- **Railway-Oriented Programming**: Use Result/Either patterns for error handling
- **Micro-Methods**: Break complex logic into composable small methods
- **One-Line Preference**: Use method chaining, lambdas, ternary operators

### Testing Strategy (3-Layer Approach)
1. **Unit Tests**: Mockito-based isolated testing
2. **Integration Tests**: TestContainers with full Spring context
3. **Manual Testing**: Postman/curl scripts for real-world validation

### Module Independence
Each chapter module must be:
- **Completely self-contained**: No dependencies on other chapters
- **Independently buildable**: Can build and run without other modules
- **Fully functional**: Complete working application at its complexity level

## Database Guidelines

### Schema Consistency
- All modules share identical database schema concepts
- Table/column names in UPPERCASE (e.g., USER, ROLE, ARTICLE)
- Boolean fields stored as ENUM in database ('ACTIVE'/'INACTIVE')
- Mandatory audit fields: CREATED_AT, UPDATED_AT, CREATED_BY, UPDATED_BY

### MariaDB 11.4.7 Features
- Use JSON columns for flexible data
- Leverage Common Table Expressions (CTEs)
- Implement Window Functions for analytics
- UTF8MB4 charset for full Unicode support

## Modern Java Practices

### Required Modern Features
- **Records**: For DTOs and value objects
- **Pattern Matching**: Switch expressions with instanceof
- **Text Blocks**: Multi-line string literals
- **Sealed Classes**: Restricted inheritance hierarchies
- **Virtual Threads**: High-performance concurrency (Project Loom)

### Functional Programming Patterns
- **Immutability**: final keywords, Records, immutable collections
- **Pure Functions**: No side effects in business logic
- **Higher-Order Functions**: Function, Predicate, Consumer composition
- **Monadic Patterns**: Optional, Stream, CompletableFuture chaining

## Coding Style Rules

### Compression Principles
- **One-line preference**: Compress logic into single expressions where possible
- **Method chaining**: Fluent interfaces and builder patterns
- **Lambda expressions**: Concise functional programming
- **Optional chaining**: Avoid null checks with Optional methods
- **Data-driven conditions**: Replace if-else with Map/Stream/Rule objects

### Comment Policy
Only add comments for:
- Complex business rules that aren't self-explanatory
- Algorithm explanations for non-trivial logic
- Integration points with external systems

## Security Implementation

### Multi-Layer Security
- **Transport Layer**: HTTPS/SSL with PKCS12 certificates
- **Authentication**: OAuth2 (Google, Facebook, GitHub, Kakao)
- **XSS Protection**: Lucy XSS Filter integration
- **CSRF Protection**: Token-based request validation
- **SQL Injection**: MyBatis parameter binding

### Role-Based Access Control
```
ADMINISTRATOR → Full system access
MANAGER → Content and limited user management
USER → Personal profile and content creation
```

## Internationalization Strategy

All user-facing messages must support i18n:
- Use MessageSource for all text
- Key format: `domain.action.detail` (e.g., `user.validation.nickname.invalid`)
- Support Korean (default) and English
- Validation messages integrated with Bean Validation

## Error Handling Patterns

### Railway-Oriented Programming
- Use Result<T, E> types for explicit success/failure handling
- Chain operations with flatMap for fail-fast behavior
- Implement recovery strategies with fallback methods
- Avoid exceptions for business logic failures

## Performance Optimization

### Database Optimization
- HikariCP connection pooling
- Query optimization with proper indexing
- Redis distributed caching
- Read replica strategies for scalability

### Application Optimization
- Async processing with @Async
- Lazy loading strategies
- Static resource CDN integration
- JVM tuning for G1GC

## Testing Guidelines

### TestContainers Integration
- MariaDB 11.4.7 containers for integration tests
- Reusable test configurations in `BaseTestConfiguration`
- Schema initialization via schema.sql
- Test data isolation per test method

### Test Structure
```java
@SpringBootTest
@Import(BaseTestConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class IntegrationTest {
    // Test implementation
}
```

## Build and Deployment

### Quality Gates
- All tests must pass before build succeeds
- JaCoCo coverage thresholds enforced
- Static analysis (SpotBugs, Checkstyle, PMD)
- Security vulnerability scanning

### Environment Consistency
- Development: MariaDB 11.4.7 Docker
- Testing: TestContainers MariaDB 11.4.7
- Production: MariaDB 11.4.7 cluster

## Template Rendering Testing Strategy

### Separate Template Testing Approach

Since this project uses MyBatis (not JPA/Hibernate), template rendering tests are separated from controller logic tests to avoid complex dependency issues.

#### 1. Template Unit Tests (Isolated)
```java
@SpringBootTest
@ActiveProfiles("test")
class ThymeleafTemplateTest {
    @Autowired
    private TemplateEngine templateEngine;
    
    @Test
    void articleListTemplateTest() {
        Context context = new Context();
        context.setVariable("articles", mockArticles);
        String result = templateEngine.process("article/list", context);
        assertTrue(result.contains("expected content"));
    }
}
```

#### 2. Controller Integration Tests (Full Flow)
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ArticleControllerIntegrationTest {
    
    private void setupCustomUserDetails() {
        User testUser = User.builder()
            .id(1L).nickname("Test").roles(List.of(userRole)).build();
        PrimaveraUserDetails userDetails = PrimaveraUserDetails.of(testUser);
        // Set SecurityContext...
    }
    
    @Test
    void articleListFullFlowTest() {
        setupCustomUserDetails();
        mockMvc.perform(get("/articles"))
               .andExpect(status().isOk())
               .andExpect(view().name("article/list"));
    }
}
```

#### 3. Template Existence Tests (Verification)
```java
@SpringBootTest
class TemplateExistenceTest {
    @Test
    void articleTemplatesExist() {
        assertTemplateExists("templates/article/list.html");
        assertTemplateExists("templates/article/detail.html");
    }
}
```

#### 4. E2E Tests with Selenium (Optional)
```bash
# Run E2E tests only when needed
./gradlew test -Dselenium.test=true
```

### Template Testing Best Practices

- **Separation of Concerns**: Controller logic tests vs Template rendering tests
- **Custom UserDetails**: Use PrimaveraUserDetails instead of @WithMockUser for template tests
- **Mock Services**: Mock business logic, focus on template rendering
- **Template Validation**: Verify essential elements are rendered correctly
- **Responsive Testing**: Test templates on different screen sizes (E2E)

## Git Workflow and File Management

### Commit Message Format
```
feat: add OAuth2 social login integration
fix: resolve MySQL connection timeout issue
docs: update API documentation
test: add integration tests for user service
refactor: extract payment processing logic
```

### Atomic Commits
- One logical change per commit
- Small, focused commits for easy review
- Meaningful commit messages explaining the "why"

### File Management Guidelines

#### No Backup Files Policy
- **Git Version Control**: Use Git as the primary backup and version control system
- **No Manual Backups**: Never create manual backup files (*.bak, *.backup, *.old, *_backup)
- **No Duplicate Files**: Avoid creating copies like `Article.java.old`, `config.yml.backup`
- **Git History**: Rely on Git history for file versioning and recovery
- **Branch Strategy**: Use branches for experimental changes instead of backup files

**Bad Examples (Never Create):**
```
❌ ArticleMapper.java.bak
❌ application.yml.backup
❌ schema.sql.old
❌ UserService_backup.java
❌ config-backup/
❌ test_old/
```

**Good Practices:**
```
✅ Use Git branches: git checkout -b feature/new-implementation
✅ Use Git stash: git stash save "temporary work"
✅ Use Git tags: git tag v1.0.0
✅ Use commit history: git log --oneline
✅ Use Git revert: git revert <commit-hash>
```

#### Git-Based Version Management
- **Feature Branches**: Create branches for new features or experiments
- **Commit Frequently**: Make small, incremental commits
- **Descriptive Messages**: Write clear commit messages explaining changes
- **Tag Releases**: Use Git tags for version marking
- **Revert Strategy**: Use `git revert` instead of keeping old files

#### IDE and Editor Settings
Configure your IDE to avoid creating backup files:

**IntelliJ IDEA:**
```
File → Settings → Editor → General
☐ Safe delete (with confirmation)
☐ Create backup files (*.~)
```

**VS Code (.vscode/settings.json):**
```json
{
  "files.hotExit": "off",
  "files.autoSaveDelay": 1000,
  "files.exclude": {
    "**/*.bak": true,
    "**/*.backup": true,
    "**/*.old": true,
    "**/*_backup*": true
  }
}
```

**Eclipse:**
```
Window → Preferences → General → Workspace
☐ Build automatically
☐ Save automatically before build
```

#### .gitignore Best Practices
Ensure .gitignore prevents accidental backup file commits:

```gitignore
# Backup files (prevent accidental commits)
*.bak
*.backup
*.old
*_backup*
*.orig
*.tmp
*~

# IDE backup files
*.swp
*.swo
.#*
\#*#

# OS backup files
.DS_Store?
.DS_Store
Thumbs.db
```

#### Code Review Guidelines
- **Backup File Detection**: Review process should catch and reject backup files
- **Clean Commits**: Ensure commits contain only intended changes
- **No Temporary Files**: Reject commits with temporary or backup files
- **Consistent Naming**: Follow project naming conventions strictly

## Module Evolution Strategy

### Educational Duplication Policy
- **Concept Progression**: Same concepts implemented with increasing complexity
- **Independent Learning**: Each module teachable without prerequisites
- **Real-world Simulation**: Shows how codebases evolve over time
- **Comparison Learning**: Multiple implementation approaches demonstrated

This project prioritizes educational value over traditional DRY principles, allowing intentional duplication to demonstrate concept evolution and different implementation approaches across the learning progression.

## Detailed Coding Guidelines

### Senior Spring Boot Developer Standards

Follow these guidelines as a senior Spring Boot developer:

#### Core Development Principles
- **Test-Driven Development**: Always write tests before implementation (Red-Green-Refactor cycle)
- **Domain-Driven Design**: Design around business domains and ubiquitous language
- **Clean Code**: Prioritize readability and maintainability above all else
- **SOLID Principles**: Strictly adhere to Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion
- **Design Patterns**: Apply appropriate patterns for flexible and maintainable structure

#### Layer Separation and Architecture
- **Clear Layer Separation**: Controller-Service-Repository with distinct responsibilities
- **Interface-Based Programming**: Program to interfaces, not implementations
- **Dependency Injection**: Leverage Spring's IoC container effectively
- **Immutable Objects**: Prefer immutable objects and minimize side effects

#### Modern Java Technology Usage
- **Records**: Use for DTOs and value objects instead of traditional POJOs
- **Pattern Matching**: Leverage switch expressions and instanceof pattern matching
- **Text Blocks**: Use for multi-line string literals
- **Stream API**: Adopt functional programming style extensively
- **Optional**: Ensure null safety with Optional chaining
- **CompletableFuture**: Implement asynchronous programming patterns
- **Virtual Threads**: Use for high-performance concurrency (Project Loom)
- **Sealed Classes**: Implement restricted inheritance hierarchies
- **Local Variable Type Inference (var)**: Use type inference appropriately

#### Functional Programming Strategy

##### Immutability Principles
- **final keywords**: Use final for variables that shouldn't change
- **Record classes**: Prefer Records over traditional POJOs for data classes
- **Immutable collections**: Use List.of(), Set.of(), Map.of() for immutable collections
- **Defensive copying**: Create defensive copies when exposing internal state

##### Pure Functions and Higher-Order Functions
- **Pure Functions**: Design functions without side effects
- **Function Composition**: Use andThen(), compose() for method chaining
- **Higher-Order Functions**: Leverage Function, Predicate, Consumer, Supplier extensively
- **Method References**: Prefer method references over lambda expressions when possible

##### Monadic Patterns
- **Optional Chaining**: Use map(), flatMap(), orElse() instead of null checks
- **Stream Processing**: Leverage filter(), map(), reduce() for data transformation
- **CompletableFuture**: Chain asynchronous operations with thenApply(), thenCompose()

#### Railway-Oriented Programming

##### Error Handling Strategy
- **Either Pattern**: Implement Result<T, E> types for explicit success/failure handling
- **Chaining Operations**: Use flatMap() for continuous processing pipelines
- **Failure Propagation**: Implement automatic short-circuit on first failure
- **Combinators**: Use map(), flatMap(), orElse() for result composition
- **Exception Safety**: Replace exceptions with type-safe error handling
- **Recovery Strategies**: Implement recover() and fallback() methods for alternative paths
- **Validation Accumulation**: Accumulate multiple validation results for comprehensive feedback

#### Micro-Method Strategy

##### Method Decomposition
- **Atomic Responsibility**: Each method should have one clear, single responsibility
- **Function Decomposition**: Break complex logic into multiple small functions
- **Composition Over Complexity**: Combine small methods to build complex functionality
- **Test Isolation**: Small units enable better test isolation and debugging
- **Reusability**: Small methods can be reused across different contexts
- **Readable Intent**: Method names should clearly express their purpose
- **Precise Debugging**: Small units allow pinpoint error identification

#### Code Compression and Style

##### One-Line Preference
- **Method Chaining**: Use fluent interfaces and builder patterns extensively
- **Lambda Expressions**: Write concise functional programming expressions
- **Ternary Operators**: Compress conditional logic into single lines
- **Optional Chaining**: Use orElse(), map(), flatMap() to avoid null checks
- **Switch Expressions**: Handle multiple conditions in single expressions
- **Stream Operations**: Process collections functionally in one pipeline

##### Data-Driven Conditionals
- **Rule Engine Pattern**: Externalize complex business rules into data structures
- **Condition Mapping**: Use Map, Enum, Record for condition-result mapping
- **Strategy Pattern Data**: Manage conditional execution logic as Function objects
- **Decision Trees**: Convert nested conditions into hierarchical data structures
- **DSL Approach**: Create domain-specific languages for complex rule expression
- **Configuration-Based Rules**: Manage business rules externally via YAML/JSON

#### Database and Schema Guidelines

##### Schema Consistency
- **Module Schema Alignment**: Maintain identical database schemas across all modules
- **Common Entity Models**: Keep consistent domain models (with independent module implementations)
- **Unified Schema File**: Use single schema.sql shared across the entire project
- **Data Consistency**: Ensure identical table structures and relationships across modules
- **Boolean to Enum Conversion**: Store boolean attributes as database ENUMs ('ACTIVE'/'INACTIVE', 'ENABLED'/'DISABLED')

##### Audit and History Management
- **History Tables**: Create corresponding history tables for all entities
- **Change Tracking**: Record creation, modification, deletion timestamps and actors
- **JPA Auditing**: Use @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
- **Envers Integration**: Implement Hibernate Envers for automatic version management
- **Event Sourcing**: Store all changes as events for state reconstruction capability
- **Soft Delete**: Use logical deletion with deleted_at fields to preserve history
- **Audit Logging**: Implement complete audit trails (who, when, what, why)

##### Schema Management Automation
- **Migration Tools**: Use Flyway/Liquibase for version-based schema migrations
- **JPA DDL Automation**: Leverage spring.jpa.hibernate.ddl-auto for schema generation
- **ERD Auto-Generation**: Automatically convert JPA entities to ERD diagrams
- **Schema Documentation**: Auto-extract table/column comments for documentation
- **Version Control**: Manage migration scripts through Git history
- **CI/CD Integration**: Automate schema updates and ERD refresh during deployment
- **Schema Validation**: Verify production and development environment schema consistency
- **MariaDB 11.4.7 Optimization**: Utilize version-specific performance improvements and features

#### Configuration and Internationalization

##### Configuration Management
- **External Configuration**: Use application.yml/properties for environment-specific settings
- **Type-Safe Binding**: Implement @ConfigurationProperties for type-safe configuration
- **Constants Management**: Use constant classes instead of magic numbers/strings
- **Enum Usage**: Ensure type safety for fixed values with enums
- **Profile-Based Configuration**: Separate development/test/production environments

##### Internationalization Strategy
- **Multi-Language Messages**: Implement messages.properties for Korean/English support
- **MessageSource Integration**: Use Spring's internationalization features extensively
- **Error Message Key System**: Use consistent error.domain.field key naming convention
- **Locale-Based Responses**: Respond based on Accept-Language header or user settings
- **Validation Message Integration**: Connect Bean Validation with multi-language messages
- **Exception Message Internationalization**: Apply internationalization to all exception messages

#### Testing Strategy Implementation

##### Multi-Layer Testing Structure
1. **Mock-Based Unit Tests**
   - Use Mockito for dependency mocking and isolated unit testing
   - Apply @MockBean for Spring context bean mocking
   - Focus on pure business logic verification
   - Ensure fast feedback with no external dependencies

2. **Integration Tests**
   - Use TestContainers with Docker MariaDB 11.4.7
   - Load full Spring context with @SpringBootTest
   - Verify end-to-end scenarios and complete workflows
   - Ensure data consistency with schema.sql-based isolated test environments
   - Maintain exact production environment parity with MariaDB 11.4.7

3. **External Tool-Based Operational Tests**
   - Create manual test collections with Postman/Insomnia for REST APIs
   - Implement command-line API tests with curl scripts
   - Perform performance and load testing with JMeter
   - Conduct real-world validation through developer-performed end-to-end testing

4. **Build-Time Automated Testing**
   - Execute all tests automatically during Maven/Gradle test phase
   - Halt build process immediately upon test failures
   - Measure code coverage with JaCoCo verification
   - Generate detailed test results and coverage reports

#### Module Independence Strategy

##### Complete Isolation
- **Independent Execution**: Each module must run independently without other modules
- **Self-Contained Completeness**: Include all necessary classes and configurations within each module
- **Interface-Only Communication**: Use standard interfaces only for inter-module communication
- **Event-Based Communication**: Implement loose coupling through Spring Events and Message Queues
- **Independent Deployment**: Enable individual module builds and deployments
- **Separate Testing**: Perform module-specific independent testing
- **Configuration Separation**: Manage independent configuration files per module
- **Dependency Inversion**: Prevent upper modules from depending on lower modules

#### Educational Duplication Strategy

##### Progressive Learning Approach
- **Chapter-by-Chapter Evolution**: Gradually expand functionality of the same classes across modules
- **Learning Path Tracking**: Visualize concept development from basic to advanced
- **Independent Completeness**: Each module functions as a complete independent system at its complexity level
- **Duplication Over DRY**: Prioritize educational effectiveness over DRY principles
- **Progressive Complexity**: Start simple in early modules, gradually increase complexity in later modules
- **Comparative Learning**: Present various implementation approaches across different modules
- **Real-World Simulation**: Reproduce actual code evolution processes found in development

#### Quality Assurance and Build Strategy

##### Build Quality Assurance
- **Comprehensive Test Execution**: Automatically run all unit/integration/E2E tests during builds
- **Failure-Triggered Build Halt**: Immediately stop build process if any test fails
- **Coverage Threshold Verification**: Fail builds that don't meet minimum coverage requirements
- **Code Quality Gates**: Require passing SpotBugs, Checkstyle, PMD static analysis
- **Security Scanning**: Include dependency vulnerability checks
- **Performance Testing**: Include basic performance validation with JMeter
- **Parallel Test Execution**: Execute tests in parallel to reduce build time
- **MariaDB 11.4.7 Testing**: Use TestContainers to ensure exact version environment for database tests

#### Database Environment Standardization

##### MariaDB 11.4.7 Consistency
- **Version Lock**: Use identical MariaDB 11.4.7 version across all modules
- **Docker Container Management**: Automatically manage MariaDB 11.4.7 containers with TestContainers
- **Environment Consistency**: Unify development, testing, and production environments with MariaDB 11.4.7
- **Latest Feature Utilization**: Leverage MariaDB 11.4.7's JSON, CTE, Window Functions
- **Performance Optimization**: Use MariaDB 11.4.7's improved indexing and query optimization features
- **Character Set Standards**: Use utf8mb4 character set for complete Unicode support
- **Timezone Configuration**: Default to UTC, convert at application level when necessary

#### Environment Separation Strategy

##### Local vs Test Environment Distinction
- **Complete Environment Separation**: Maintain strict separation between local development and automated testing environments
- **Local Development Environment**: Use localhost MariaDB 11.4.7 for interactive development and debugging
- **Test Environment**: Use TestContainers MariaDB 11.4.7 for automated integration testing and CI/CD
- **Build Environment**: Ensure all builds use isolated TestContainers for consistent, reproducible results

##### Local Development Environment
- **Purpose**: Interactive development, debugging, and manual testing by developers
- **Database**: localhost MariaDB 11.4.7 (Docker or native installation)
- **Configuration**: Use `application-local.yml` with fixed localhost connection settings
- **Data Persistence**: Maintain data across application restarts for development continuity
- **Schema Management**: Use Flyway migrations for schema versioning and updates
- **Profile Activation**: Use `local` profile for development environment
- **Execution Methods**: Multiple ways to run with local profile (detailed below)

##### Profile-Based Database Auto-Selection Strategy

**Core Principle: Single Profile, Automatic Database Selection**
- **`local` Profile**: Automatically uses localhost Docker MariaDB 11.4.7
- **`test` Profile**: Automatically uses TestContainers MariaDB 11.4.7
- **No Manual Configuration**: Database environment selected based on active profile only

**Profile-Based Execution Methods:**

**1. Local Development (localhost Docker MySQL):**
```bash
# Gradle with local profile
./gradlew :chapXX:bootRun -Dspring.profiles.active=local

# Direct JAR execution
java -Dspring.profiles.active=local -jar build/libs/chapXX.jar

# IDE setup with local profile
-Dspring.profiles.active=local
```

**2. Test Execution (TestContainers MySQL):**
```bash
# All tests with TestContainers (default)
./gradlew :chapXX:test

# Specific test with TestContainers
./gradlew :chapXX:test --tests ArticleMapperProfileTest

# Force test profile
./gradlew :chapXX:test -Dspring.profiles.active=test
```

**2. IDE Configuration:**

**IntelliJ IDEA Setup:**
- Go to `Run/Debug Configurations`
- Select your Spring Boot application
- In `Environment Variables` or `VM Options`, add:
  ```
  -Dspring.profiles.active=local
  ```
- Or in `Program Arguments`, add:
  ```
  --spring.profiles.active=local
  ```

**Visual Studio Code Setup:**
- Create/modify `.vscode/launch.json`:
  ```json
  {
    "version": "0.2.0",
    "configurations": [
      {
        "type": "java",
        "name": "Spring Boot Local",
        "request": "launch",
        "mainClass": "com.genius.primavera.HierarchicalCommentApplication",
        "vmArgs": "-Dspring.profiles.active=local",
        "projectName": "chapXX"
      }
    ]
  }
  ```

**Eclipse Setup:**
- Right-click project → `Run As` → `Run Configurations`
- Select your application under `Java Application`
- In `Arguments` tab, add to `VM arguments`:
  ```
  -Dspring.profiles.active=local
  ```

**3. Environment Variable Setup:**
```bash
# Linux/macOS - Add to ~/.bashrc or ~/.zshrc
export SPRING_PROFILES_ACTIVE=local

# Windows - System Environment Variables
set SPRING_PROFILES_ACTIVE=local

# Or PowerShell
$env:SPRING_PROFILES_ACTIVE="local"
```

**4. Docker MariaDB 11.4.7 Setup for Local Development:**
```bash
# Start MariaDB container for local development
docker run -d \
  --name mariadb-primavera-local \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3306:3306 \
  --restart=unless-stopped \
  mariadb:11.4.7

# Verify container is running
docker ps | grep mariadb-primavera-local

# Check logs if needed
docker logs mariadb-primavera-local

# Connect to MariaDB for debugging (optional)
docker exec -it mariadb-primavera-local mysql -u primavera -p primavera
```

**5. Local Development Workflow:**
```bash
# 1. Start MariaDB container (if not running)
docker start mariadb-primavera-local

# 2. Verify MariaDB connectivity
telnet localhost 3306

# 3. Run application with local profile
./gradlew :chap11:bootRun -Pprofile=local

# 4. Application will start with local profile settings
# Check logs for profile confirmation:
# "The following profiles are active: local"
```

**6. Profile Verification:**
- Check application logs for profile activation:
  ```
  INFO  --- [main] c.g.p.HierarchicalCommentApplication : The following profiles are active: local
  ```
- Verify database connection in logs:
  ```
  INFO  --- [main] com.zaxxer.hikari.HikariDataSource  : HikariPool-1 - Starting...
  INFO  --- [main] com.zaxxer.hikari.HikariDataSource  : HikariPool-1 - Start completed.
  ```

**7. Troubleshooting Local Profile Issues:**

**Common Issues:**
- **MariaDB not running**: Start Docker container
- **Port 3306 occupied**: Check `docker ps` and stop conflicting containers
- **Connection refused**: Verify MariaDB container health with `docker logs`
- **Profile not activated**: Check application logs for active profiles

**Debug Commands:**
```bash
# Check if MariaDB port is open
netstat -an | grep 3306

# Test MariaDB connection
mysql -h localhost -P 3306 -u primavera -p

# Check Docker container status
docker inspect mariadb-primavera-local

# View Spring Boot actuator info (if enabled)
curl http://localhost:8080/actuator/env | grep profiles
```

##### Test Environment (Build/CI)
- **Purpose**: Automated integration testing, continuous integration, and build verification
- **Database**: TestContainers MariaDB 11.4.7 (automatically managed Docker containers)
- **Configuration**: Use `application-testcontainer.yml` with dynamic TestContainers properties
- **Data Isolation**: Each test gets a fresh, isolated database instance
- **Schema Management**: Use `schema.sql` initialization scripts for fast test setup
- **Execution Command**: `./gradlew :chapXX:test` (TestContainers auto-start/stop)
- **CI/CD Compatibility**: No external dependencies required, works in any Docker-enabled environment

##### Profile-Based Configuration Management

**Profile Priority and Activation Rules:**
- **Default Profile**: `application.yml` (fallback configuration)
- **Local Profile**: `application-local.yml` (development environment, localhost Docker MySQL)
- **Test Profile**: `application-test.yml` (test environment, TestContainers MySQL)
- **Profile Override**: Local/Test profiles override default settings
- **Environment Variables**: `SPRING_PROFILES_ACTIVE` takes highest priority

**Configuration File Structure:**
```
src/main/resources/
├── application.yml              # Default configuration
├── application-local.yml        # Local development (localhost Docker MySQL)
├── application-default.yml      # Production settings
└── application-prod.yml         # Production overrides

src/test/resources/
└── application-test.yml         # Test environment (TestContainers MySQL)
```

##### Environment-Specific Configuration

**Local Environment Setup:**
```yaml
# application-local.yml
spring:
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mysql://localhost:3306/primavera?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: primavera
    password: primavera
  flyway:
    enabled: true  # Use Flyway for schema management
    encoding: UTF-8
    fail-on-missing-locations: false
```

**Test Environment Setup:**
```yaml
# application-test.yml
spring:
  application:
    name: primavera-test
  flyway:
    enabled: false  # Use schema.sql instead
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
```

##### TestContainers Integration Standards

**Container Configuration:**
```java
@Container
static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
    .withDatabaseName("primavera")
    .withUsername("primavera")
    .withPassword("primavera")
    .withInitScript("sql/schema.sql");
```

**Dynamic Property Configuration:**
```java
@DynamicPropertySource
static void configureTestProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mariadb::getJdbcUrl);
    registry.add("spring.datasource.username", mariadb::getUsername);
    registry.add("spring.datasource.password", mariadb::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
}
```

##### Profile-Based Test Code Organization

**Unified Integration Test Structure (Recommended):**
```java
@ProfileBasedIntegrationTest
@ActiveProfiles("test")  // or "local"
@DisplayName("Your Integration Test Description")
class YourIntegrationTest {
    
    @Autowired
    private YourMapper yourMapper;
    
    @Test
    @DisplayName("Test description")
    void testMethod() {
        // Test automatically uses appropriate database based on profile
        // - test profile: TestContainers MariaDB 11.4.7
        // - local profile: localhost Docker MariaDB 11.4.7
    }
}
```

**Legacy Direct TestContainers Structure (Alternative):**
```java
@SpringBootTest(classes = TestApplication.class)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@Transactional
@Rollback(false)
class YourIntegrationTest {
    
    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
        .withDatabaseName("primavera")
        .withUsername("primavera")
        .withPassword("primavera")
        .withInitScript("sql/schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
    }
}
```

**Profile-Based Test Application Configuration:**
```java
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
@MapperScan("com.genius.primavera.domain.mapper")
class TestApplication {
    // Profile-aware test application - automatically selects database
}
```

##### Build Integration Requirements

- **Local Development**: Developers must have localhost MariaDB 11.4.7 running for `bootRun` tasks
- **Automated Testing**: All `test` tasks must use TestContainers exclusively
- **CI/CD Pipeline**: Build servers require only Docker for TestContainers, no external MySQL needed
- **Module Independence**: Each module's tests run in complete isolation with their own TestContainers
- **Performance Optimization**: Use TestContainers reuse feature where appropriate to reduce test execution time
- **Database Version Consistency**: Both local and test environments must use identical MariaDB 11.4.7 version

##### Environment Validation

**Local Environment Validation:**
- Verify localhost:3306 MySQL connectivity before development
- Ensure Flyway migrations run successfully
- Validate schema compatibility with production environment

**Test Environment Validation:**
- Confirm TestContainers can start MariaDB 11.4.7 successfully
- Verify schema.sql initialization works correctly
- Validate test isolation and data cleanup between tests

This environment separation strategy ensures development efficiency while maintaining test reliability and CI/CD compatibility.

#### Specific Implementation Rules

##### Database Naming Conventions
- **Table and Column Names**: Use UPPERCASE for all database table and column names
- **Key Naming**: Use UPPERCASE for all database key names
- **Consistent Naming**: Apply uniform naming conventions across all modules

##### Comment Policy
- **Essential Logic Only**: Add comments only for essential core logic and complex business rules
- **Concise Comments**: Write brief, clear comments when necessary
- **Avoid Over-Commenting**: Don't comment obvious code or standard patterns

#### Technology Stack Integration

##### Core Technologies
- **Spring Boot**: Progress from basic to advanced concepts
- **Java 21+**: Utilize latest LTS features extensively
- **MariaDB 11.4.7**: Use Docker TestContainers for version consistency
- **Testing**: JUnit 5, Mockito, TestContainers integration
- **Security**: Spring Security with internationalization support
- **ORM**: Spring Data JPA with Auditing functionality
- **Version Management**: Hibernate Envers for automatic versioning
- **Schema Migration**: Flyway/Liquibase for database migrations
- **Documentation**: SchemaSpy/ERDocs for automatic ERD generation
- **Contract Testing**: Spring Cloud Contract for inter-module testing
- **Coverage**: JaCoCo for code coverage measurement
- **Static Analysis**: SpotBugs, Checkstyle, PMD for code quality
- **Operational Testing**: Postman, JMeter for real-world testing
- **Build Automation**: Maven/Gradle with comprehensive test automation

This comprehensive coding guideline ensures consistent, high-quality code across all modules while maintaining the educational project's unique requirements and constraints.