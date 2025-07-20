# Project Structure

## Maven Standard Layout
```
├── pom.xml                    # Maven configuration and dependencies
├── README.md                  # Project documentation (Chinese)
├── test.http                  # HTTP request examples for API testing
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java      # Spring Boot main class
│   │   │   ├── TestController.java       # Demo REST controller
│   │   │   ├── configuration/            # Spring configuration classes
│   │   │   ├── controller/               # REST controllers
│   │   │   ├── dto/                      # Data Transfer Objects
│   │   │   │   └── UserDto.java
│   │   │   ├── entity/                   # JPA/Database entities
│   │   │   │   └── User.java
│   │   │   ├── exception/                # Custom exceptions and handlers
│   │   │   ├── mapper/                   # MapStruct mapping interfaces
│   │   │   │   └── UserMapper.java
│   │   │   ├── service/                  # Business logic layer
│   │   │   │   └── impl/                 # Service implementations
│   │   │   └── vo/                       # Value Objects for validation
│   │   │       └── DelayVo.java
│   │   └── resources/
│   │       └── application.properties    # Application configuration
│   └── test/                             # Test classes (mirrors main structure)
└── target/                               # Maven build output
```

## Package Organization

### Core Principles
- **Layered Architecture**: Clear separation between controller, service, and data layers
- **Package by Feature**: Group related classes together when the project grows
- **Standard Spring Boot Conventions**: Follow Spring Boot naming and structure patterns

### Package Responsibilities
- **controller/**: REST endpoints and request handling
- **service/**: Business logic and transaction management
- **entity/**: Database entities and domain models
- **dto/**: Data transfer objects for API communication
- **vo/**: Value objects for validation and data binding
- **mapper/**: Object mapping between layers (MapStruct)
- **configuration/**: Spring configuration classes
- **exception/**: Custom exceptions and global error handling

## Naming Conventions
- **Controllers**: End with `Controller` (e.g., `UserController`)
- **Services**: End with `Service` (e.g., `UserService`)
- **Entities**: Simple nouns (e.g., `User`, `Order`)
- **DTOs**: End with `Dto` (e.g., `UserDto`, `CreateUserDto`)
- **VOs**: End with `Vo` (e.g., `DelayVo`, `SearchVo`)
- **Mappers**: End with `Mapper` (e.g., `UserMapper`)

## File Organization
- **test.http**: Contains API testing examples with variables
- **README.md**: Comprehensive documentation in Chinese
- **application.properties**: Environment-specific configuration
- Empty packages are preserved for future expansion