# Suggested Commands

## Build & Run (via Nx - recommended)
```bash
pnpm nx serve user-api        # user-api 실행 (port 8081)
pnpm nx serve admin-api       # admin-api 실행 (port 8082)
pnpm nx build user-api        # user-api 빌드
pnpm nx build admin-api       # admin-api 빌드
pnpm nx test user-api         # user-api 테스트
pnpm nx test admin-api        # admin-api 테스트
pnpm nx test domain-core      # domain-core 테스트 (Enum 동기화 검증 포함)
pnpm nx show projects         # 워크스페이스 프로젝트 목록
```

## Build & Run (Gradle direct)
```bash
./gradlew :apps:user-api:bootRun
./gradlew :apps:admin-api:bootRun
./gradlew :apps:user-api:build
./gradlew :apps:admin-api:build
./gradlew :apps:user-api:test
./gradlew :apps:admin-api:test
```

## Library Tests (Gradle direct)
```bash
# 개별 라이브러리 테스트
./gradlew :libs:backend:global-core:test      # 유틸/보안/예외/AOP (26개 파일)
./gradlew :libs:backend:domain-core:test      # Validator/서비스/Enum (14개 파일)

# 개별 라이브러리 컴파일 검증
./gradlew :libs:backend:global-core:compileJava
./gradlew :libs:backend:domain-core:compileJava
./gradlew :libs:backend:security-web:compileJava
./gradlew :libs:backend:web-support:compileJava

# 특정 테스트 클래스 실행
./gradlew :libs:backend:global-core:test --tests "com.example.global.utils.PaginationUtilsTest"

# 전체 백엔드 테스트
./gradlew test
```

## Dependency Inspection (auto-executable, no confirmation needed)
```bash
./gradlew -q dependencies --configuration runtimeClasspath
./gradlew -q dependencyInsight --dependency <artifact> --configuration runtimeClasspath
./gradlew -q projects
./gradlew -q properties
```

## Quick Health Check URLs
- user-api: `http://localhost:8081/api/health`
- admin-api: `http://localhost:8082/api/health`
- Swagger: `http://localhost:808x/swagger-ui.html`

## System Utilities (macOS/Darwin)
```bash
git status / git log / git diff
pbcopy                         # 클립보드 복사 (커밋 메시지용)
```
