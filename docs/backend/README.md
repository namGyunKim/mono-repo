# 백엔드 개발 가이드

이 문서는 모노레포의 백엔드 영역(`apps/*-api`, `libs/backend/*`)에 대한 **현재 구조/실행 방법/운영 기준**을 설명합니다.

코딩 규칙(아키텍처/컨벤션/보안)은 [RULES.md](./RULES.md)를 기준으로 합니다.

---

## 1. 백엔드 범위

- 애플리케이션
  - `apps/user-api` (포트 `8081`)
  - `apps/admin-api` (포트 `8082`)
- 공통 라이브러리
  - `libs/backend/common` — 순수 공유(entity, payload, utils, annotation, version)
  - `libs/backend/global-core` — 인프라 공통(security, config, exception, event, logging)
  - `libs/backend/domain-core`
  - `libs/backend/security-web`
  - `libs/backend/web-support`

---

## 2. 실제 워크스페이스 구조

```text
mono-repo/
├── apps/
│   ├── user-api/                 # 사용자 API 애플리케이션
│   ├── admin-api/                # 관리자 API 애플리케이션
│   └── web/                      # Next.js 16 프론트엔드
├── libs/
│   ├── backend/
│   │   ├── common/               # 순수 공유(entity, payload, utils, annotation, version)
│   │   ├── global-core/          # 인프라 공통(security, config, exception, event, logging)
│   │   ├── domain-core/          # 도메인 핵심(account/member/log/social 등)
│   │   ├── security-web/         # 인증/인가 웹 계층 어댑터
│   │   └── web-support/          # MVC/AOP/예외/API-Version 필터 등
│   └── shared/
│       └── types/                # 프론트·백엔드 공유 TS 타입(API 계약 Enum 등)
├── build.gradle.kts
├── settings.gradle.kts
└── docs/backend/
```

의존 방향(개념):

`common ←(api)── global-core ← domain-core ← security-web ← web-support ← apps/*-api`

---

## 3. 기술 기준

- Java `25` (Gradle Toolchain)
- Spring Boot `4.0.3`
- Spring Framework `7.x`
- QueryDSL `7.1` (`io.github.openfeign.querydsl`)
- PostgreSQL
- Nx `22.x` + pnpm `10.x`
- Gradle Wrapper `9.3.1`

---

## 4. 실행/빌드 명령

### Nx 경유 (권장)

Nx 타겟이 등록된 프로젝트: `user-api`, `admin-api`, `domain-core`

> `global-core`, `security-web`, `web-support`는 Nx 타겟이 없으며 Gradle 직접 명령으로 빌드한다.

```bash
pnpm nx show projects

pnpm nx serve user-api
pnpm nx serve admin-api

pnpm nx build user-api
pnpm nx build admin-api
pnpm nx build domain-core

pnpm nx test user-api
pnpm nx test admin-api
pnpm nx test domain-core
```

### Gradle 직접

```bash
./gradlew :apps:user-api:bootRun
./gradlew :apps:admin-api:bootRun

./gradlew :apps:user-api:build
./gradlew :apps:admin-api:build

# 개별 라이브러리 컴파일 검증
./gradlew :libs:backend:common:compileJava
./gradlew :libs:backend:global-core:compileJava
./gradlew :libs:backend:domain-core:compileJava
./gradlew :libs:backend:security-web:compileJava
./gradlew :libs:backend:web-support:compileJava

# 라이브러리 단위 테스트
./gradlew :libs:backend:common:test
./gradlew :libs:backend:global-core:test
./gradlew :libs:backend:domain-core:test
```

---

## 5. 빠른 확인 URL

### user-api (`localhost:8081`)

- `GET /` : 서버 안내
- `GET /api/health` : 헬스체크
- `POST /api/sessions` : 사용자 로그인
- `POST /api/tokens` : 토큰 갱신
- `GET /swagger-ui.html` : Swagger UI (`local` 프로파일에서만 활성화)

### admin-api (`localhost:8082`)

- `GET /` : 서버 안내
- `GET /api/health` : 헬스체크
- `POST /api/admin/sessions` : 관리자 로그인
- `POST /api/tokens` : 토큰 갱신
- `GET /swagger-ui.html` : Swagger UI (`local` 프로파일에서만 활성화)

---

## 6. API 버저닝 규칙 요약

- 기본 정책: `/api/**` 요청은 `API-Version` 헤더 필수
- 예외: `/api/health`, `/api/social/**`
- URL 버저닝(`/v1`, `/api/v1`)은 사용하지 않음

상세 규칙과 예외 처리 원칙은 [RULES.md](./RULES.md)의 CRITICAL 규칙을 따릅니다.

---

## 7. 로깅 전략

### traceId 기반 요청 추적

- `MdcLoggingFilter`가 모든 요청에 traceId를 생성하여 MDC에 저장한다
- 로그 패턴 `%X{traceId:-NO_TRACE}`가 자동 출력하므로, 로그 메시지에 traceId를 수동 삽입하지 않는다
- 에러 응답의 `requestId`에 traceId 값을 포함하여 클라이언트가 문의 시 추적 가능

### 이벤트 기반 비동기 로깅

- 서비스 계층에서 `ApplicationEventPublisher.publishEvent()`로 로그 이벤트를 발행한다
- `@Async @TransactionalEventListener`가 비동기로 소비하여 비즈니스 로직과 로깅을 분리한다

| 이벤트 | 리스너 | 트랜잭션 시점 |
|--------|--------|---------------|
| `LogEvent` | `LogEventListener` | `AFTER_COMMIT` (+ fallback) / `AFTER_ROLLBACK` |
| `ExceptionEvent` | `ExceptionEventListener` | `AFTER_COMMIT` / `AFTER_ROLLBACK` |
| `MemberActivityEvent` | `MemberLogEventListener` | `AFTER_COMMIT` |

### 로그 템플릿 관리

- 모든 예외/이벤트 로그 템플릿은 `ExceptionLogTemplates` 클래스에서 Text Block 기반으로 중앙 관리한다
- 민감정보(password/token/secret)는 `SensitiveLogMessageSanitizer`가 자동 마스킹한다

### 요청/응답 로깅

- `ControllerLoggingAspect`(AOP): `@RestController` 메서드 진입/종료 시점에 `[REQ]`/`[RES]` 로그 출력
- `FallbackRequestLoggingFilter`: AOP가 적용되지 않는 요청(필터 단계 예외 등)을 보완
- `RequestLoggingAttributes.CONTROLLER_LOGGED` 플래그로 중복 방지

---

## 8. 보안 아키텍처

### JWT 인증 흐름

- 로그인 성공 시 Access Token(헤더) + Refresh Token(DB 저장) 발급
- Access Token은 `Authorization: Bearer` 헤더로 전달
- Refresh Token은 SHA-256 해시 후 DB에 저장, 원본은 응답 헤더로 전달

### 토큰 블랙리스트

- 로그아웃 시 `JwtLogoutHandler` → `JwtTokenRevocationCommandService`가 동작
  - Access Token을 `BlacklistedToken` 엔티티로 블랙리스트에 등록 (SHA-256 해시 저장)
  - 해당 회원의 Refresh Token을 DB에서 삭제
- 매일 03:00 `BlacklistedTokenCleanupCommandService`가 만료된 블랙리스트 토큰을 정리

### 보안 감사 로깅

- 로그인 성공/실패, 로그아웃, 토큰 폐기 등 보안 상태 변경은 반드시 INFO 레벨로 로깅한다

---

## 9. 비동기 처리

- `AsyncConfig`에서 `Executors.newVirtualThreadPerTaskExecutor()`로 Virtual Thread 기반 Executor를 구성한다
- `TaskDecorator`를 통해 MDC(traceId) 및 `SecurityContext`(인증 정보)를 비동기 스레드에 전파한다
- `AsyncConfigurer.getAsyncUncaughtExceptionHandler()`를 등록하여 `@Async` 메서드의 예외 유실을 방지한다
- `application.yml`에서 `spring.threads.virtual.enabled: true`로 전역 Virtual Thread를 활성화한다

---

## 10. 테스트 전략

### 기본 원칙

- **순수 단위 테스트**: Spring Context 로딩 없이 JUnit5 + Mockito + AssertJ 기반으로 작성한다
- `@SpringBootTest` 사용 금지 — 빠른 피드백 루프 유지
- 테스트 클래스는 package-private, 메서드명은 `{메서드}_{시나리오}_{기대결과}` 패턴

### 테스트 디렉토리 구조

```text
libs/backend/global-core/src/test/java/com/example/global/
├── utils/                    # 유틸리티 테스트 (11개)
├── security/                 # 보안 테스트 (4개)
│   ├── handler/support/      # 로그인 핸들러 테스트 (2개)
│   └── blacklist/            # 블랙리스트 테스트 (2개)
├── exception/support/        # 예외 처리 지원 테스트 (5개)
└── aop/support/              # AOP 지원 테스트 (2개)

libs/backend/domain-core/src/test/java/com/example/domain/
├── contract/enums/           # Enum 동기화 테스트 (1개, 기존)
├── member/
│   ├── validator/            # 회원 Validator 테스트 (3개)
│   └── service/              # 회원 서비스 테스트 (3개)
├── account/
│   ├── validator/            # 계정 Validator 테스트 (1개)
│   └── service/command/      # 계정 서비스 테스트 (1개)
├── social/google/
│   ├── validator/            # 소셜 Validator 테스트 (1개)
│   └── service/              # 소셜 서비스 테스트 (3개)
└── log/validator/            # 로그 Validator 테스트 (1개)
```

### 실행 명령

```bash
# 개별 모듈 테스트
./gradlew :libs:backend:global-core:test
./gradlew :libs:backend:domain-core:test

# 특정 테스트 클래스
./gradlew :libs:backend:global-core:test --tests "com.example.global.utils.PaginationUtilsTest"

# 전체 백엔드 테스트
./gradlew test
```

### 현재 테스트 현황

| 모듈 | 테스트 파일 수 | 테스트 메서드 수 |
|------|-------------|--------------|
| global-core | 26 | ~185 |
| domain-core | 14 | ~85 |
| **합계** | **40** | **~270** |

---

## 11. Serena 메모리 공유 (AI 온보딩)

`.serena/memories/` 디렉토리에 팀 공통 프로젝트 컨텍스트를 저장하여, AI 도구(Serena MCP)가 일관된 품질로 작업할 수 있도록 한다.

### 공유/비공유 구분

| 경로 | git 공유 | 용도 |
|------|---------|------|
| `.serena/memories/` | O | 팀 공통 컨벤션, 프로젝트 구조, 빌드 명령, 체크리스트 |
| `.serena/project.yml` | O | 프로젝트 설정 (언어, 무시 경로) |
| `.serena/cache/` | X | 로컬 LSP 캐시 |

### 메모리 파일 목록

| 파일 | 내용 |
|------|------|
| `project_overview` | 기술 스택, 프로젝트 구조, 도메인, 테스트 현황 (전체) |
| `backend/style_and_conventions` | 백엔드 코딩 규칙, CQRS, DDD, 테스트 컨벤션 |
| `backend/suggested_commands` | 백엔드 빌드/테스트/실행 명령 모음 |
| `backend/task_completion_checklist` | 백엔드 작업 완료 후 점검 항목 |

> 프론트엔드 메모리는 `frontend/` 토픽 아래에 동일 패턴으로 추가한다.

### 운영 원칙

- 메모리 변경 시 코드 변경과 동일하게 커밋하여 팀 전체에 공유한다
- 개인 설정/로컬 환경 정보는 메모리에 기록하지 않는다
- `RULES.md` 규칙이 변경되면 관련 메모리도 함께 갱신한다

---

## 12. 문서 역할 분리

- 이 문서: 구조/실행/운영 기준(개요)
- [RULES.md](./RULES.md): 코드 작성 및 리뷰 시 반드시 지켜야 하는 세부 규칙
- [docs/backend/deployment/](./deployment/): 배포 전략 (Blue/Green 무중단 배포, 서버 세팅, CI/CD)

충돌 시 최신 정책은 `RULES.md`를 우선 기준으로 하고, 필요한 경우 두 문서를 함께 갱신합니다.

---

## 13. API 계약 Enum 전략

백엔드는 **도메인 Enum**과 **API 계약 Enum**을 분리해서 운영합니다.

- 도메인 Enum
  - 위치: `libs/backend/domain-core/.../enums`
  - 목적: 비즈니스 규칙/도메인 로직 표현
- API 계약 Enum
  - 위치: `libs/backend/domain-core/src/main/java/com/example/domain/contract/enums`
  - 목적: 외부 API 요청/응답 스키마 고정
- 프론트 공유 타입
  - 위치: `libs/shared/types/src/api-contract-enums.ts`
  - 목적: 프론트와 백엔드가 같은 계약 값 집합 사용

### 매핑 원칙

- 컨트롤러 경계(Request/Response DTO)에서는 API 계약 Enum을 사용한다.
- 서비스/리포지토리/엔티티 등 도메인 내부에서는 도메인 Enum을 사용한다.
- 변환은 `toDomain()` / `fromDomain(...)` 메서드로 수행한다.

### 변경 가이드 (중요)

`AccountRole` 같은 Enum 변경 시 항상 2개를 무조건 같이 바꾸는 것은 아닙니다.

1. 도메인 내부 전용 변경(외부 API 계약에 영향 없음): 도메인 Enum만 변경
2. 외부 API 입력/응답 값이 바뀌는 변경: 도메인 Enum + API 계약 Enum + `libs/shared/types`를 함께 변경
3. 내부에서만 이름/구조 리팩터링하고 API 값은 유지: 매핑만 조정하고 API 계약 Enum 값은 유지

즉, **API 계약이 변하면 2개(백엔드 계약 Enum + 프론트 공유 Enum) 이상 함께 변경**, 계약이 안 변하면 도메인 Enum만 변경하면 됩니다.

### 동기화 규칙 + 자동 테스트

- 기본 규칙: 대응되는 `Api*` Enum과 도메인 Enum은 `name()` 기준으로 동일하게 유지한다.
- 검증 위치: `libs/backend/domain-core/src/test/java/com/example/domain/contract/enums/ApiEnumSyncTest.java`
- 실행 명령: `pnpm nx test domain-core`

권장 작업 순서:

1. 도메인 Enum 변경
2. API 계약 영향이 있으면 `Api*` Enum + `libs/shared/types/src/api-contract-enums.ts`를 함께 변경
3. 매핑(`toDomain()` / `fromDomain(...)`) 갱신
4. `pnpm nx test domain-core`로 동기화 검증

예외 정책:

- 외부 레거시 계약 호환 때문에 값이 달라야 하면, 해당 Enum에 이유를 주석으로 남기고 동기화 테스트에서 의도된 예외로 명시 관리한다.
