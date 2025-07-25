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
./gradlew :chap11:test --tests MySQLContainerTest

# Run application (specific chapter)
./gradlew :chap09:bootRun

# Build with parallel execution
./gradlew build --parallel
```

### Database Operations
```bash
# Start MySQL 8.4.0 with Docker
docker run -d --name mysql-primavera \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=primavera \
  -e MYSQL_USER=primavera \
  -e MYSQL_PASSWORD=primavera \
  -p 3306:3306 mysql:8.4.0

# TestContainers automatically manages MySQL 8.4.0 for tests
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
- **Production**: MySQL 8.4.0 (standardized across all modules)
- **Testing**: TestContainers with MySQL 8.4.0 (ensures environment consistency)
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

### MySQL 8.4.0 Features
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
- MySQL 8.4.0 containers for integration tests
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
- Development: MySQL 8.4.0 Docker
- Testing: TestContainers MySQL 8.4.0
- Production: MySQL 8.4.0 cluster

## Git Workflow

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
- **MySQL 8.4.0 Optimization**: Utilize version-specific performance improvements and features

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
   - Use TestContainers with Docker MySQL 8.4.0
   - Load full Spring context with @SpringBootTest
   - Verify end-to-end scenarios and complete workflows
   - Ensure data consistency with schema.sql-based isolated test environments
   - Maintain exact production environment parity with MySQL 8.4.0

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
- **MySQL 8.4.0 Testing**: Use TestContainers to ensure exact version environment for database tests

#### Database Environment Standardization

##### MySQL 8.4.0 Consistency
- **Version Lock**: Use identical MySQL 8.4.0 version across all modules
- **Docker Container Management**: Automatically manage MySQL 8.4.0 containers with TestContainers
- **Environment Consistency**: Unify development, testing, and production environments with MySQL 8.4.0
- **Latest Feature Utilization**: Leverage MySQL 8.4.0's JSON, CTE, Window Functions
- **Performance Optimization**: Use MySQL 8.4.0's improved indexing and query optimization features
- **Character Set Standards**: Use utf8mb4 character set for complete Unicode support
- **Timezone Configuration**: Default to UTC, convert at application level when necessary

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
- **MySQL 8.4.0**: Use Docker TestContainers for version consistency
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