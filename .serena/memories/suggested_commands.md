# Suggested Commands

## Build
- `./gradlew build` - Full build
- `./gradlew :libs:backend:domain-core:compileJava` - Compile domain-core module only

## Test
- `pnpm nx test` - Run tests via NX
- `./gradlew test` - Run tests via Gradle

## Lint/Format
- `pnpm nx lint` - Lint via NX

## NX
- `pnpm nx build` - Build via NX
- `pnpm nx run-many --target=build` - Build all projects
- `pnpm nx affected --target=test` - Test affected projects

## Git
- `git status` - Check working tree status
- `git diff` - View changes
