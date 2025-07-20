# Technology Stack

## Core Framework
- **Spring Boot 3.5.0** - Main application framework
- **Java 17** - Programming language (minimum version)
- **Maven** - Build system and dependency management

## Key Dependencies
- **Spring Boot Web** - REST API development
- **Spring Boot Validation** - Bean validation with annotations
- **Spring Boot Data Redis** - Redis caching integration
- **Spring Boot JOOQ** - Type-safe SQL query builder
- **H2 Database** - In-memory database for development/testing
- **Lombok** - Boilerplate code reduction
- **MapStruct 1.6.2** - Compile-time object mapping
- **Testcontainers** - Integration testing with Docker containers

## Build Commands

### Development
```bash
# Clean and compile
mvn clean compile

# Run application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Testing
```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Integration tests only
mvn test -Dtest="*IT"
```

### Packaging
```bash
# Create JAR
mvn clean package

# Skip tests during packaging
mvn clean package -DskipTests

# Create executable JAR
mvn clean install
```

## Configuration
- **Port**: 8080 (configurable via `server.port`)
- **Database**: H2 in-memory (`jdbc:h2:mem:testdb`)
- **Profiles**: Support for Spring profiles (dev, test, prod)

## Code Generation
- MapStruct processors run during compilation
- Lombok annotations processed at compile time
- JOOQ code generation can be configured via Maven plugin