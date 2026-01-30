# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build (skip tests)
./gradlew clean build -x test

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.example.studyroomreservation.SomeTestClass"

# Run specific test method
./gradlew test --tests "com.example.studyroomreservation.SomeTestClass.testMethodName"

# Run application (local profile with H2)
./gradlew bootRun

# Run with dev profile (requires MySQL)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Tech Stack

- Java 21, Spring Boot 3.5.9, Spring Data JPA, Spring Security
- QueryDSL 5.0.0 for type-safe queries
- MapStruct 1.5.5 for DTO mapping
- Thymeleaf for admin web forms
- MySQL (production), H2 (local/test)
- P6Spy for SQL logging

## Architecture Overview

### Package Structure
```
com.example.studyroomreservation
├── domain/           # Domain modules (member, payment, refund, reservation, room)
│   └── {module}/
│       ├── controller/   # REST & view controllers
│       ├── dto/          # request/response DTOs
│       ├── entity/       # JPA entities with rich domain logic
│       ├── mapper/       # MapStruct mappers
│       ├── repository/   # Spring Data JPA repositories
│       ├── service/      # Application services
│       ├── validation/   # Custom validators
│       └── web/          # Form factories for Thymeleaf
└── global/           # Cross-cutting concerns
    ├── common/       # Base entity classes
    ├── config/       # Spring configurations
    ├── exception/    # Exception handling
    ├── filter/       # Servlet filters
    ├── security/     # Security configuration
    └── util/         # Utilities
```

### Entity Inheritance Hierarchy
```
BaseIdEntity (id)
└── BaseCreatedEntity (createdAt)
    ├── BaseAuditableEntity (updatedAt)
    │   └── BaseSoftDeletableEntity (deletedAt, soft delete)
    └── BasePolicyEntity (name, isActive, activeUpdatedAt)
```

### Exception Handling
- **BusinessException**: Wraps ErrorCode enum for domain-specific errors
- **GlobalExceptionHandler** (Order=2): Handles MVC controller exceptions, returns Thymeleaf error views
- **ApiExceptionHandler** (Order=1): Handles REST controller exceptions, returns JSON ApiErrorResponse
- ErrorCode enum defines HTTP status, code, and Korean message for each error case

### Validation Pattern
- DTOs use Bean Validation annotations (`@Valid`)
- Custom Spring Validators (e.g., `OperationPolicyValidator`) handle DB-dependent validation
- Controllers use `@InitBinder` to register validators, errors go to BindingResult
- Domain entities validate invariants in static factory methods, throwing BusinessException

### Key Domain Concepts
- **Room**: Study room with operation policy, room rule, refund policy
- **OperationPolicy**: Defines slot unit and weekly operation schedules (7 days required)
- **OperationSchedule**: Daily open/close times with 24h and cross-day logic support
- **RoomRule**: Booking rules per room
- **RefundPolicy/RefundRule**: Refund rate rules based on time before reservation

### MapStruct Mappers
- Use `@Mapper(componentModel = "spring")` for DI
- Naming: `XxxMapper` interface generates `XxxMapperImpl`
- Pattern: `toResponse(Entity)` for entity-to-DTO, `toEntity(RequestDto)` for DTO-to-entity

## Key Patterns

- Entities use private constructors with public static factory methods (e.g., `Room.create(...)`)
- Soft delete via `deletedAt` timestamp in `BaseSoftDeletableEntity`
- Policy entities track activation state with `isActive` and `activeUpdatedAt`
- Controllers distinguish between API (`/api/*`) and view endpoints
