# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

# Primavera - Spring Boot Educational Project

## Project Overview

Primavera is a comprehensive Spring Boot educational project demonstrating progressive learning from basic concepts to advanced microservices architecture. The project consists of 18+ modules (chap01-chap18 + utilities) designed for step-by-step learning progression.

## Quick Development Commands

### Build and Test
```bash
# Build entire project
./gradlew clean build

# Build specific module
./gradlew :chap04:build

# Run tests for specific module
./gradlew :chap04:test

# Run specific test class
./gradlew :chap04:test --tests PrimaveraServiceTest

# Run application (specific chapter)
./gradlew :chap04:bootRun

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

## Architecture & Philosophy

### Educational Progressive Learning Structure
- **chap01-04**: Core Spring Boot fundamentals (DI, Configuration, Data Access)
- **chap05-08**: Web development, templates, security basics
- **chap09-13**: Advanced features (OAuth2, security, complex data structures)
- **chap14-17**: Production concerns (reactive, microservices, monitoring)
- **chap18**: Complete microservices architecture
- **appendix**: Custom Spring Boot starters and utilities

### Key Architectural Principles
- **Educational Duplication Over DRY**: Each module is intentionally self-contained with its own complete implementation to demonstrate concept evolution
- **Module Independence**: Each chapter can be built and run independently without dependencies on other chapters
- **Progressive Complexity**: Concepts evolve from simple to complex across modules
- **Real-world Simulation**: Shows how actual codebases evolve over time

## Technology Stack

### Core Technologies
- **Spring Boot**: 3.3.6 (Latest stable)
- **Java**: 21+ (LTS with modern features)
- **Database**: MariaDB 11.4.7 (Standardized across all modules)
- **Build Tool**: Gradle with centralized dependency management
- **Testing**: JUnit 5, Mockito, TestContainers

### Key Dependencies (Centralized in gradle.properties)
- **Lombok**: 1.18.36 (Code generation)
- **MariaDB Driver**: 3.5.4
- **MyBatis**: 3.0.4 (SQL mapping framework)
- **TestContainers**: 1.21.3 (Integration testing)
- **Spring Security**: 6.4.4
- **Thymeleaf**: 3.4.0 (Template engine)

### Custom Spring Boot Starters
- **spring-boot-starter-test-container**: Automated TestContainers configuration
- **spring-boot-starter-lucy-filter**: XSS protection
- **spring-boot-starter-social-kakao**: Kakao OAuth2 integration

## Development Guidelines

### Code Quality Standards
- **Modern Java Features**: Use Records, Pattern Matching, Text Blocks, Optional chaining
- **Functional Programming**: Prefer Stream API, immutable objects, pure functions
- **Clean Architecture**: Clear separation between Controller-Service-Repository layers
- **Test-Driven Development**: Write tests before implementation

### Testing Strategy (3-Layer Approach)
1. **Unit Tests**: Mockito-based isolated testing
2. **Integration Tests**: TestContainers with full Spring context
3. **Manual Testing**: Postman/curl scripts for API validation

### Database Strategy
- **Production**: MariaDB 11.4.7
- **Testing**: TestContainers with MariaDB 11.4.7 (environment consistency)
- **Schema Management**: init-db.sql for tests, Flyway for migrations
- **Naming Convention**: UPPERCASE table/column names (e.g., USER, ROLE, ARTICLE)

## Testing Configuration

### TestContainers Integration
```yaml
# application-test.yml
primavera:
  testcontainers:
    mariadb:
      enabled: true
      dockerImageName: mariadb:11.4.7
      driverClassName: org.mariadb.jdbc.Driver
      databaseName: primavera
      username: primavera
      password: primavera
      initScript: sql/init-db.sql
```

### Test Class Structure
```java
@SpringBootTest
@EnablePrimaveraTestcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Integration Test Description")
class YourIntegrationTest {
    // TestContainers automatically configured
    // MariaDB 11.4.7 container started/stopped automatically
}
```

## Environment Configuration

### Profile-Based Configuration
- **local**: Development environment (localhost Docker MariaDB)
- **test**: Testing environment (TestContainers MariaDB)
- **default**: Production settings

### Running Applications
```bash
# Local development with profile
./gradlew :chap04:bootRun -Dspring.profiles.active=local

# Tests with TestContainers (automatic)
./gradlew :chap04:test
```

## Module-Specific Guidelines

### Version Management
All dependency versions are centralized in `gradle.properties`:
- Add new versions alphabetically in appropriate category
- Reference using `${versionVariableName}` in build.gradle files
- Never hardcode versions in individual module build.gradle files

### Build Configuration
Each module includes:
- Spring Boot Gradle plugin
- Jacoco for code coverage
- Centralized dependency management
- TestContainers integration

### Security Implementation
- **Multi-layer security**: Transport (HTTPS), Authentication (OAuth2), XSS protection
- **Role-based access control**: ADMINISTRATOR � MANAGER � USER
- **Internationalization**: Korean (default) and English support

## Coding Standards

### Method and Class Design
- **Micro-methods**: Break complex logic into small, composable methods
- **One-line preference**: Use method chaining, lambdas, ternary operators
- **Immutability**: Prefer final keywords, Records, immutable collections
- **Null Safety**: Use Optional extensively instead of null checks

### Comment Policy
Only add comments for:
- Complex business rules that aren't self-explanatory
- Algorithm explanations for non-trivial logic
- Integration points with external systems
- **Avoid over-commenting**: Don't comment obvious code

## Git Workflow

### Atomic Commits
- One logical change per commit
- Small, focused commits for easy review
- Meaningful commit messages explaining the "why"

### Commit Message Format
```
feat: add OAuth2 social login integration
fix: resolve MariaDB connection timeout issue
docs: update API documentation
test: add integration tests for user service
refactor: extract payment processing logic
```

### File Management Policy
- **Git as Primary Version Control**: Never create manual backup files
- **No Backup Files**: Avoid *.bak, *.backup, *.old, *_backup files
- **Use Git Features**: Branches for experiments, tags for releases, history for recovery

## Performance & Quality

### Quality Gates
- All tests must pass before build succeeds
- JaCoCo coverage thresholds enforced
- Static analysis tools integrated
- Security vulnerability scanning

### Database Optimization
- HikariCP connection pooling
- Proper indexing strategies
- Query optimization with MyBatis
- MariaDB 11.4.7 specific features (JSON, CTEs, Window Functions)

## Best Practices

### Educational Project Considerations
- **Learning Over Efficiency**: Prioritize educational value over traditional DRY principles
- **Concept Demonstration**: Show multiple approaches to the same problem across different modules
- **Independent Modules**: Each module should be a complete, working example at its complexity level

### Modern Development Patterns
- **Railway-Oriented Programming**: Use Result/Either patterns for error handling
- **Event-Driven Architecture**: Implement loose coupling through Spring Events
- **Configuration as Code**: Externalize configuration, use type-safe binding

This project serves as a comprehensive learning resource for Spring Boot development, from basic concepts to production-ready microservices architecture.