# Mono-Repo Project Overview

## Purpose
NX monorepo containing a Spring Boot backend with hexagonal/DDD architecture.

## Tech Stack
- **Build**: Gradle (Kotlin DSL) + NX workspace + pnpm
- **Backend**: Spring Boot, Java 25, Lombok, JPA
- **Architecture**: Hexagonal (Port/Adapter), DDD, CQRS

## Structure
- `libs/backend/domain-core/` - Domain core (entities, support classes, ports)
- `apps/` - Applications
- `docs/backend/` - Backend development guidelines (RULES.md, README.md)

## Key Commands
- Build: `./gradlew build` or `pnpm nx build`
- Compile: `./gradlew :libs:backend:domain-core:compileJava`
- Test: `pnpm nx test`

## Conventions
- Korean log messages
- `@Slf4j`, `@RequiredArgsConstructor`
- No unnecessary Javadoc
- Follow `docs/backend/RULES.md` for all backend changes
