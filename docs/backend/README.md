# 백엔드 REST API 베이스 프로젝트

> ⚠️ **중요: member_log 파티셔닝 적용 범위**
> - `member_log`는 **실운영/장기 데이터 보관 환경에서 월 단위 파티셔닝을 전제로** 설계되어 있습니다.
> - 운영 DB는 **PostgreSQL 17**을 전제로 합니다.
> - member_log 파티션 관련 작업은 **수동으로 docs/db의 SQL을 실행**하세요.
>   - `docs/db/member_log_partitioning.sql`
>   - `docs/db/member_log_partitions.sql`
> - 파티션이 없으면 해당 월의 로그 INSERT가 실패할 수 있습니다.
> - 베이스 프로젝트는 **로컬/샘플 환경에서 JPA `create-drop`을 전제로** 하므로,
>   이 경우 파티션 SQL은 적용 대상이 아니며(테이블이 매번 재생성됨), 실운영 전에는 DDL 자동 생성을 반드시 분리/비활성화해야 합니다.
> - **베이스 프로젝트 특성상 기존 데이터 고려는 필요 없으므로** 초기 전환 시 데이터 이관은 생략해도 됩니다.

Spring Boot 기반의 **REST API 전용 베이스(Starter) 프로젝트**입니다.
새 프로젝트를 시작할 때 반복되는 구성(응답 포맷/예외 처리/보안/로깅/Swagger/JPA/QueryDSL 등)을 미리 세팅해 둔 형태입니다.

> 이 프로젝트는 "도메인 확장형 스타터"를 지향합니다.
> 현재는 예제 도메인(회원/계정/로그/소셜 로그인 등)이 포함되어 있으며, 필요에 따라 제거/확장해서 사용합니다.
> **베이스 프로젝트 특성상 기본 실행은 JPA `create-drop`을 전제로 합니다.**

---

## 모노레포 구조

```
mono-repo/
├── apps/
│   ├── user-api/               # Spring Boot 4.0.3 (Java 25) — 사용자 도메인 API
│   ├── (product-api/)          # 향후 추가
│   └── (order-api/)            # 향후 추가
├── build.gradle.kts            # Gradle 루트 — 공통 의존성/플러그인
├── settings.gradle.kts         # Gradle 루트 — 서브프로젝트 include
├── gradle/wrapper/             # Gradle 9.3.1 Wrapper
├── gradlew / gradlew.bat
└── docs/backend/               # 백엔드 공통 문서
```

각 API 앱은 루트 `build.gradle.kts`의 공통 설정을 상속받고, 앱별 `build.gradle.kts`에서 전용 의존성을 추가합니다.

### 스크립트 보호 규칙

- `scripts/` 경로 및 `.sh` 파일은 절대 수정하지 않습니다.

---

## 빠르게 확인할 URL (user-api 기준)

- Root(서버 안내): `GET http://localhost:8081/`
- Health Check: `GET http://localhost:8081/api/health`
- Swagger UI(Local): `GET http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: 보안상 기본 비활성화 (`/v3/api-docs`)

---

## 기술 스택

- Java **25** (Gradle Toolchain)
- Spring Boot **4.0.3**
- Spring Framework **7.x**
- Jackson **3** (`tools.jackson.*` 기준, 어노테이션은 `com.fasterxml.jackson.annotation.*`)
- Spring Security (JWT 헤더 기반 인증/STATELESS + 메서드 보안 `@PreAuthorize`)
- Spring Data JPA (Hibernate) + QueryDSL 5 (jakarta)
- Springdoc OpenAPI/Swagger UI
- PostgreSQL
- (옵션) P6Spy
- AWS SDK v2 (S3)

---

## Java 25 문법/플랫폼 우선 원칙

- 신규 코드/리팩토링은 **Java 25 언어/플랫폼 개선사항을 기본값**으로 사용합니다.
- 언어/모델링
  - 데이터 전달/불변 모델: `record`
  - 타입 분기: Pattern Matching(`instanceof`, `switch`) + `switch expression`
  - Primitive 패턴 매칭(JEP 507, Preview): preview 활성화 환경에서 검토
  - Module Import Declarations(JEP 511): 도구성/실험성 코드에서만 검토, 운영 코드는 명시적 import 우선
  - Compact Source Files/Instance Main(JEP 512): 스파이크/샘플에 한정
  - 생성자 안전성: Flexible Constructor Bodies(JEP 513) 스타일 우선
  - 멀티라인 문자열: Text Block(`""" ... """`) + `formatted(...)`
  - 타입 추론(`var`)은 가독성 저하가 없을 때만 제한적으로 사용
- 동시성/컨텍스트
  - 요청 컨텍스트 전달은 `ThreadLocal`보다 `ScopedValue`(JEP 506) 우선
  - I/O 바운드 병렬 작업은 Virtual Thread + Structured Concurrency(JEP 505) 조합 우선 검토
- 성능/운영
  - AOT cache(JEP 514/515), JFR(JEP 518/520), Compact Object Headers(JEP 519), Generational Shenandoah(JEP 521) 검토
- 보안: 키 파생 로직은 JDK KDF API(JEP 510) 우선
- AI/수치: Vector API(JEP 508) 벤치마크 기반 적용
- Preview/Incubator/Experimental 기능은 기본 비활성, 적용 시 `--enable-preview`/운영 영향 검토 선행

---

## Spring 7 / Boot 4 릴리즈 반영 메모

- 실행 기준 JDK는 Java 25
- JSON 처리는 Jackson 3(`tools.jackson.*`) 기준
- 외부 연동 우선순위: `공식 SDK 우선` → `@HttpExchange` → `@EnableHttpServices` 검토
- 단순/정적 조회는 Spring Data 파생 쿼리 우선, 복잡/동적 조회는 QueryDSL 강제
- null 계약은 Bean Validation + 타입 시그니처로 명시

### Spring Framework 7 우선 사용 항목

- API 버저닝: `version = ApiVersioning.*` 스타일 우선, 헤더 문자열 직접 분기 금지
- 외부 HTTP 연동: `@HttpExchange` 기본, 다수 인터페이스 시 `@EnableHttpServices` 검토
- 입력 검증: Bean Validation + `@InitBinder` + `@RestControllerAdvice` 통일
- null 안정성: `@Nullable`/`Optional` 시그니처 계약

### Spring Boot 4 우선 사용 항목

- 베이스라인: `Java 25 + Spring Boot 4 + Spring Framework 7`, 미만 버전 호환성 타협 금지
- HTTP Service Clients 자동구성, 신규 `RestTemplate` 도입 지양
- API 버저닝: `spring.mvc.apiversion.*` 속성 우선 검토
- I/O 바운드: `spring.threads.virtual.enabled` 활용 검토
- 관측성: `spring-boot-starter-opentelemetry` 기반 우선
- 프로퍼티 마이그레이션 시 이름 변경 확인

---

## Gradle 의존성 관리

- 버전/좌표 관리는 `gradle/libs.versions.toml`(Version Catalog) 사용 예정
- 의존성 잠금은 `gradle.lockfile`, `settings-gradle.lockfile`로 관리
- 의존성 추가/버전 변경 후에는 잠금 파일 갱신:
- 샌드박스/권한 차단 시 도구 escalation을 즉시 수행
- 실행 실패 시 `--no-daemon`으로 1회 재시도 후, 계속 차단되면 escalation 수행
- 캐시는 기본 사용, 결과 불일치 의심 시에만 `--refresh-dependencies` 1회 허용
- 결과 보고 시 캐시 기준인지 refresh 기준인지 명시

```bash
./gradlew dependencies --write-locks
./gradlew compileJava
```

---

## 공통 응답/에러 포맷

### 성공 응답

```json
{
  "data": { "...": "..." }
}
```

### 에러 응답

```json
{
  "code": "1001",
  "message": "인증이 필요합니다.",
  "requestId": "e9845b25",
  "errors": []
}
```

---

## 인증/인가 개요

- Spring Security 기반 **JWT 헤더 인증** (STATELESS)
- `/api/**`는 기본 인증 필수, 권한은 `@PreAuthorize`로 통제
- `@CurrentAccount` 파라미터로 현재 로그인 사용자 정보 주입
- 인증 여부/권한 확인은 **`MemberGuard`** 가드 메서드로 통합

### JWT 헤더 정책

- Access Token: `Authorization: Bearer {accessToken}`
- Refresh Token: `X-Refresh-Token: {refreshToken}`
- 로그인/리프레시 토큰은 **응답 헤더로만** 전달

### 인증 API 요약

| API | 메서드 | 경로 |
|-----|--------|------|
| 로그인(일반) | `POST` | `/api/sessions` |
| 로그인(관리자) | `POST` | `/api/admin/sessions` |
| 리프레시 | `POST` | `/api/tokens` |

### 관리자 전용 API 규칙

- 클래스명: `Admin` 접두어
- URL: `/api/admin/**`

---

## 테스트 전략

- TDD는 선택적 적용 (도메인 복잡도/회귀 위험에 따라)
- 테스트 대상 우선순위: 핵심 도메인 규칙 > 보안 > 데이터 무결성
- 단순 매핑/보일러플레이트는 최소 범위
- 설정 변경이 필요하면 `/sample-application/**` 하위 샘플 설정 파일만 수정 대상으로 사용

## 로깅/관측성 메모

- 예외 로그 템플릿 변경은 `ExceptionLogTemplates`에서만 관리합니다.

---

## 도메인 경계 의존 원칙

- 기본값: 타 도메인 Entity/Repository 직접 참조가 아닌 `id` + Command/Query DTO 전달
- 타 도메인 조회: Query Service(또는 Port 인터페이스) 통해 연동
- 타 도메인 상태 변경: 도메인 이벤트(AFTER_COMMIT) 기반 우선
- 순환 의존 및 타 도메인 내부 정책 해석 분기 코드 금지

---

## 로컬 실행

### 요구사항

- JDK 25
- PostgreSQL (로컬)

### 실행 (NX 경유)

```bash
pnpm nx serve user-api
```

### 실행 (Gradle 직접)

```bash
./gradlew :apps:user-api:bootRun --args='--spring.profiles.active=local' -Duser.timezone=Asia/Seoul
```

---

## 예제 도메인

Spring Boot 4 / Spring Framework 7의 **API Versioning(헤더 기반)** 사용:

- 요청 헤더: `API-Version`
- 미지정 시 기본값: `0.0` (유효하지 않음)
- 프론트엔드는 반드시 `1.0`을 명시적으로 전송
- `/api/**` 요청은 `API-Version` 헤더 필수 (예외: `/api/health`, `/api/social/**`)
- URL 경로 버전 세그먼트(`/v1`, `/api/v1`) 사용 금지
- Swagger 문서에서도 `/api/health`, `/api/social/**` 제외 API는 `API-Version`을 `required=true`로 표기

### 예제 엔드포인트

- Health: `/api/health`
- 계정: `/api/accounts/**`
- 세션: `/api/sessions/**`
- 토큰: `/api/tokens`
- 회원(관리자): `/api/admin/members/**`
- 활동 로그: `/api/admin/logs/**`
- 소셜 로그인: `/api/social/**`

---

## Swagger (OpenAPI)

보안상 Swagger UI와 OpenAPI JSON은 기본 비활성화:
- `springdoc.swagger-ui.enabled=false`
- `springdoc.api-docs.enabled=false`
- 문서 활성화 시 `/api/health`, `/api/social/**`를 제외한 API는 `API-Version` 헤더를 필수(required=true)로 표시
