# 백엔드 REST API 개발 규칙

> CRITICAL: 코드 작성/수정이 끝나면 답변 전에 이 지침서(`docs/backend/RULES.md`)를 다시 확인해 규칙 누락이 없는지 최종 점검한다.
> CRITICAL: 코드/문서 수정 작업 완료 답변에는 반드시 아래 규칙 형식의 **커밋 메시지 1줄**을 함께 제공한다.

이 문서는 AI가 이 모노레포의 **REST API 전용 백엔드**(Spring Boot) 코드를 생성하거나 수정할 때 반드시 따라야 할 규칙입니다.
본 프로젝트는 `apps/*-api`와 `libs/backend/*`를 분리한 모노레포 구조를 기준으로 운영합니다.

## 개발 철학 (Development Philosophy) — CRITICAL

이 프로젝트는 아래 6가지 원칙을 핵심 개발 방향으로 삼는다.
**모든 코드 생성/수정/리뷰 시 아래 원칙을 기준으로 판단**하며, 위반 발견 시 즉시 수정한다.

| 원칙 | 핵심 요약 |
|------|-----------|
| **SRP** (단일 책임 원칙) | 클래스·메서드는 하나의 책임만 가진다 |
| **Clean Code** | 읽기 쉬운 이름, 짧은 메서드, 명확한 의도 |
| **CQRS** | Command(상태 변경)와 Query(조회)를 물리적으로 분리 |
| **DDD** | Bounded Context 경계, Aggregate, 도메인 중심 설계 |
| **Hexagonal Architecture** | Port/Adapter로 도메인을 인프라에서 격리 |
| **AI 친화적 구조** | 예측 가능한 패턴, 일관된 네이밍, 자기 문서화 코드 |

### AI 자율 실행 규칙 (CRITICAL)

- 코드 생성/수정/리팩토링/삭제는 **권한 확인 없이 자율 진행**한다
- 빌드·테스트·린트 실행도 자율 진행
- 리팩토링 중 발견된 위반 사항은 즉시 수정한다
- 코드 변경으로 **새로운 패턴·규칙·컨벤션이 확립**되면 본 지침서(`RULES.md`)에 즉시 반영한다
- 커밋/푸시는 **사용자 명시 요청 시에만** 진행

> 모노레포 경로: `apps/user-api/`, `apps/admin-api/`, `libs/backend/*`

---

## 모노레포 프로젝트 구조

```
mono-repo/
├── apps/
│   ├── user-api/               # Spring Boot 4.0.3 (Java 25)
│   ├── admin-api/              # Spring Boot 4.0.3 (Java 25)
│   └── web/                    # Next.js 16 (App Router)
├── libs/
│   ├── backend/
│   │   ├── global-core/
│   │   ├── domain-core/
│   │   ├── security-web/
│   │   └── web-support/
│   └── shared/types/           # 공유 TS 타입
├── gradle/wrapper/             # Gradle 9.3.1 Wrapper
├── build.gradle.kts            # Gradle 루트 (백엔드 공통)
├── settings.gradle.kts         # Gradle 서브프로젝트 include
├── nx.json                     # NX 설정
├── package.json / pnpm-workspace.yaml
└── docs/backend/               # 백엔드 상세 가이드
    ├── README.md               # 백엔드 프로젝트 개요
    └── RULES.md                # 백엔드 개발 규칙 (본 문서)
```

## 기술 스택 하한 (CRITICAL)

| 영역               | 기준                                       |
|------------------|------------------------------------------|
| Java             | **25** (Gradle Toolchain)                |
| Spring Boot      | **4.0.3**                                |
| Spring Framework | **7.x**                                  |
| Gradle           | **9.3.1** (Wrapper)                      |
| Node.js          | **23.x**                                 |
| Next.js          | **16.x**                                 |
| NX               | **22.x**                                 |
| QueryDSL         | **7.1** (`io.github.openfeign.querydsl`) |

## 빌드 명령

```bash
# 백엔드
pnpm nx build user-api
pnpm nx serve user-api
pnpm nx test user-api
pnpm nx build admin-api
pnpm nx serve admin-api
pnpm nx test admin-api

# Gradle 직접 실행
./gradlew :apps:user-api:build
./gradlew :apps:user-api:bootRun
./gradlew :apps:user-api:test
./gradlew :apps:admin-api:build
./gradlew :apps:admin-api:bootRun
./gradlew :apps:admin-api:test
```

## 새 백엔드 API 추가 절차

1. `apps/{name}-api/` 디렉토리를 `user-api`와 동일 구조로 생성
2. `settings.gradle.kts`에 `include("apps:{name}-api")` 추가
3. `apps/{name}-api/project.json` 생성 (NX 연동)
4. `apps/{name}-api/build.gradle.kts` 생성
5. 포트 번호 변경 (`8082`, `8083`, ...)

## 커밋 메시지 규칙 (Conventional Commits, CRITICAL)

- 기본 형식: **`type(scope): 변경 요약`**
- 예시: `docs(backend): 백엔드 지침서 구조·경로 정합성 정리`
- `type` 허용값: `feat`, `fix`, `refactor`, `docs`, `chore`
- 변경 영역이 명확하면 `scope`를 반드시 포함한다
- 권장 `scope`: `backend`, `user-api`, `admin-api`, `domain-core`, `global-core`, `security-web`, `web-support`, `web`
- `:` 뒤 설명은 **한국어**로 작성
- 커밋 메시지는 변경사항을 요약한 **제목 한 줄 1개만** 제공 (여러 후보/여러 줄 금지)
- 코드/문서 수정 작업을 완료해 사용자에게 보고할 때, 위 형식의 커밋 메시지를 **답변에 반드시 포함**한다.

---

## 도메인 지침 점검 요청 (우선 적용)

- 사용자가 "점검"을 요청하면 **개발 철학 6대 원칙**(SRP, Clean Code, CQRS, DDD, Hexagonal Architecture, AI 친화적 구조)을 기준으로 해당 도메인의 코드/설계를 점검하고 결과를 보고한다.
- 점검 시 각 원칙별로 위반 여부를 확인하며, README 및 본 지침의 세부 규칙도 함께 대조한다.
- 점검 범위가 불명확하면 **추정하지 말고 먼저 질문**한다.
- 점검 결과 보고 형식(필수)
  - `요약`: 준수/위배/보류 여부 한 줄 요약
  - `위배 항목`: 항목별로 **우선순위(높음/중간/낮음)**, 근거 규칙, 위치(파일), 설명, 권장 조치 포함
    - 각 위배 항목에는 **`선택번호`(1부터 순번)** 부여
  - `불확실/질문`: 범위나 의도가 불명확하면 추정하지 말고 질문

| 선택번호 | 우선순위 | 규칙/근거 | 위치 | 설명 | 권장 조치 |
|------|------|------|-----|------|--------|
| 1 | 높음 | 예: API-Version 헤더 규칙 | 예: `SomeApiController.java` | 예: 버전 헤더 매핑 누락 | 예: `version = ApiVersioning.V1` 추가 |

- 후속 조치 옵션 제시(필수):
  1. 높음 우선순위 항목만 우선 조치(권장)
  2. 높음 + 중간 항목까지 조치
  3. 전체 항목 일괄 조치
  4. 조치 없이 점검 결과만 확정

---

## 0. 최우선 규칙 (CRITICAL)

### 코드 제공 전 SRP/CQRS 우선 검토

- 코드를 제공하기 전에 **SRP(단일 책임 원칙)** 와 **CQRS(Command/Query 분리)** 관점에서 설계를 우선 점검한다.
- SRP/CQRS 위배 가능성이 있으면 **이유와 대안을 먼저 설명**하고, 합의된 방향으로 코드를 제공한다.

### README 확인

- 작업 전 **`docs/backend/README.md`를 반드시 읽고** 전제/정책을 준수한다.
- README와 본 문서/사용자 요청이 **충돌하거나 모호하면 즉시 질문**한다.
- 정책/규칙이 변경되면 본 문서와 관련 문서에 함께 반영한다.
- 정책/규칙 변경 시 `docs/backend/RULES.md`를 우선 갱신하고, 필요 시 관련 문서(README, docs)를 함께 갱신한다.

### 문서 파일 재확인

- 문서 파일(README, RULES 등)은 수정 전 최신 변경 가능성을 고려해 **반드시 다시 읽고** 수정한다.

### 버전 하한 고정 규칙 (CRITICAL)

- 프로젝트 베이스라인은 **Java 25 + Spring Boot 4 + Spring Framework 7**로 고정한다.
- 하위 버전 호환 타협/문법 다운그레이드/레거시 API 재도입은 금지한다.

### API-Version 헤더 규칙 (CRITICAL)

- `/api/**`는 `API-Version` 헤더 **필수**, `/api/health` 및 `/api/social/**`만 예외
- 컨트롤러 매핑은 버전 헤더 기반(`version = "..."`)으로 작성
- URL 버전 세그먼트(`/v1`, `/api/v1`) 사용 금지
- 예외: `/api/social/**` 콜백에 한해 URL 버저닝 허용
- 기본값 `0.0`은 유효하지 않으며, 프론트는 `1.0` 명시 전송 필수
- Swagger 문서에서도 `/api/health`, `/api/social/**` 제외 API는 `API-Version`을 `required=true`로 표기

### 경로/문서 참조 정합성 (CRITICAL)

- 문서에는 **현재 저장소에 실제 존재하는 경로/파일만** 참조한다.
- 경로를 문서에 추가할 때는 `rg --files` 등으로 존재 여부를 먼저 확인한다.

### 파일 경로 표기 규칙

- `src/main/resources/**` 하위 파일은 **전체 상대경로를 함께 명시**
- Java 파일은 패키지 선언으로 위치 확인 가능하므로 파일명만 명시 가능
- 앱 전용 코드는 `apps/*-api/**`, 공통 코드는 `libs/backend/**` 경계를 명시

### 스크립트 보호 규칙

- 현재 저장소에는 전용 `scripts/` 디렉토리가 없다.
- `gradlew`, `gradlew.bat` 외 shell 스크립트 추가/수정은 사용자 요청이 있을 때만 진행한다.

### 모노레포 Gradle 경로 규칙

- Gradle 명령 시 서브프로젝트 경로 명시: `./gradlew :apps:user-api:build`, `./gradlew :apps:admin-api:build`
- NX 경유: `pnpm nx build user-api`, `pnpm nx build admin-api`

### 설정파일 관련 의도사항

- 설정파일에 평문이 존재하거나 prod 활성화가 되어 있어도 **의도된 사항**으로 간주
- 보안/권장사항을 이유로 임의 변경 금지 (사용자 명시 요청 시만 예외)

### 테스트/설정 파일 변경 규칙

- TDD는 선택 전략, 전체 강제 아님
- 코드 변경 시 위험도 기준으로 필요하면 사전 요청 없이 테스트 추가/수정/실행 가능
- 사용자가 명시 요청하지 않는 한 **설정 파일 임의 수정 금지**
- 설정 변경 필요 시 사유/영향 범위를 먼저 설명하고 확인 후 진행

### Gradle 의존성 점검 실행 규칙 (CRITICAL)

- `./gradlew` 기반 명령은 **사전 확인 질문 없이 즉시 실행**
- 무질의 실행 허용 화이트리스트:
  - `./gradlew -q dependencies --configuration runtimeClasspath`
  - `./gradlew -q dependencyInsight --dependency <artifact> --configuration runtimeClasspath`
  - `./gradlew -q projects`
  - `./gradlew -q properties`
- 점검 순서: `runtimeClasspath` → `dependencyInsight` → 필요 시 `compileClasspath` 비교
- 샌드박스/권한 차단 시 도구 escalation을 즉시 수행
- 실행 실패 시 `--no-daemon`으로 1회 재시도 후, 계속 차단되면 escalation 수행
- 캐시는 기본 사용, 결과 불일치 의심 시에만 `--refresh-dependencies` 1회 허용
- 결과 보고 시 캐시 기준인지 refresh 기준인지 명시
- 의존성 점검 보고 필수 항목: `요청 명령`, `resolve 버전`, `선택 이유`, `위험도`, `권장 조치`

---

## 1. 기본 원칙 (Core Principles)

### 언어 및 소통

- 모든 답변, 주석, 커밋 메시지는 **한국어(Korean)**
- 본 문서는 **REST API 전용** — 화면(UI)/템플릿/정적 리소스 변경 금지
- 요청이 모호하면 **추정하지 말고 질문**

### 레거시/호환성 정책

- 호환성 목적의 레거시 코드를 남기지 않는다
- 변경 시 기존 방식을 제거하고 최신 규칙으로 통일

### 커밋 메시지 규칙 (CRITICAL)

- 형식은 상단 규칙과 동일하게 `type(scope): 변경 요약`을 사용
- 설명은 **한국어**, 제목 한 줄 1개만 제공
- 코드 제공 시 커밋 메시지를 함께 제공
- 커밋 메시지 제공 시 macOS `pbcopy`로 한 줄 메시지를 클립보드에 자동 복사
- 클립보드 텍스트 마지막은 개행 없이 유지 (`printf '%s' ... | pbcopy`)
- 클립보드 명령 사용이 불가한 환경이면 복사용 한 줄 코드블록을 제공

### 규칙 요약 (핵심)

- DTO는 record + from/of, 외부 `new DTO(...)` 금지
- 계층 경계는 DTO 전달 원칙 준수
- 인증/인가 판단은 MemberGuard로 통일
- 멀티라인 문자열은 Text Block 사용
- 임의 shell 스크립트 추가/수정은 사용자 요청 시에만 진행

### 클린 코드 & SRP

- 읽기 쉬운 이름, 짧은 메서드, 명확한 책임
- 가독성은 성능 미세 최적화보다 우선
- "파일 1개 = public 타입 1개" 원칙
- 중첩 깊이 2단계 이내, 초과 시 guard clause 또는 메서드 분리
- 공개 메서드 파라미터 3개 이상이면 전용 DTO 도입 고려
- 조회 메서드는 side-effect 금지, 명령 메서드는 상태 변경이 드러나는 이름
- `null` 반환 최소화, 컬렉션은 빈 컬렉션 반환, `Optional`은 반환 전용
- 계층 의존 방향: `api -> service -> repository`, 역방향/순환 금지
- 권장 기준: public 클래스 300라인, 메서드 30라인 초과 시 분리 고려
- 재할당 불필요한 변수는 **`final` 기본값**
- 람다가 단순 위임이면 메서드 레퍼런스 우선

### 기술 스택

- Java **25** (Gradle Toolchain)
- Spring Boot **4.0.3** / Spring Framework **7.x**
- 외부 연동 우선순위: 공식 SDK → `@HttpExchange` → `@EnableHttpServices`
- **Logback XML 설정 파일 미사용** (`logback.xml`, `logback-spring.xml` 등 추가/수정 금지)

### Java 25 문법 우선 지침 (CRITICAL)

- `record`, Pattern Matching(`instanceof`, `switch`), `switch expression` 우선
- Primitive 패턴 매칭(JEP 507): preview 환경에서 검토
- Module Import(JEP 511): 도구성 코드에서만, 운영 코드는 명시적 import
- Compact Source Files(JEP 512): 스파이크/샘플에 한정
- Flexible Constructor Bodies(JEP 513) 스타일 우선
- Text Block(`""" ... """`) + `formatted(...)` 우선
- `ScopedValue`(JEP 506) > `ThreadLocal`
- Virtual Thread + Structured Concurrency(JEP 505) 우선 검토
- AOT cache(JEP 514/515), JFR(JEP 518/520) 활용
- KDF API(JEP 510) 우선, Vector API(JEP 508) 벤치마크 기반
- Preview/Incubator 기능은 합의 + 문서화 필수

### Spring Framework 7 우선 사용 지침 (CRITICAL)

- API 버저닝: `version = ApiVersioning.*` 사용, 수동 헤더 분기 금지
- 외부 HTTP: `@HttpExchange` 기본, 다수 인터페이스 시 `@EnableHttpServices`
- 입력 검증: Bean Validation + `@InitBinder` + `@RestControllerAdvice`
- null 안정성: `@Nullable`/`Optional` 시그니처 계약

### Spring Boot 4 우선 사용 지침 (CRITICAL)

- HTTP Service Clients 자동구성, 신규 `RestTemplate` 지양
- `spring.threads.virtual.enabled`: p95/오류율/DB 풀 포화 기준 결정
- 관측성: `spring-boot-starter-opentelemetry` 우선
- 프로퍼티 이름 변경 확인:
  - `management.tracing.enabled` → `management.tracing.export.enabled`
  - `spring.dao.exceptiontranslation.enabled` → `spring.persistence.exceptiontranslation.enabled`

### JSON (Jackson 3) 주의사항 (CRITICAL)

- Jackson 3: 핵심 패키지 `tools.jackson.*`
- 어노테이션은 `com.fasterxml.jackson.annotation.*` 유지

```java
// ✅ 올바른 import
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.core.JacksonException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

// ❌ 금지 (컴파일 안 됨)
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
```

- `JsonNode` 문자열 추출: `stringValue()` 우선 (`asText()` deprecated 가능)
- ObjectMapper는 Spring Bean 주입, `new ObjectMapper()` 지양

### 관측성/로깅 기준

- 예외/요청 로그에 **traceId** 필수
- WARN/ERROR 로그에 핵심 컨텍스트 포함
- 민감정보(password/token/secret) 로그 금지
- `authorization` 계열 필드는 마스킹 대상
- 에러 응답 `requestId`에 traceId 값 포함
- 로그/MDC/헤더 키는 **traceId** 통일, 헤더는 `X-Trace-Id`
- 로그 템플릿은 Text Block 기반 멀티라인 우선
- 예외 로그 템플릿 변경은 `ExceptionLogTemplates`에서만 관리

---

## 2. 코딩 컨벤션 (Coding Convention)

### 객체 생성 및 변경

- ❌ Lombok `@Builder`, `@Setter`, `@Data` 금지
- ✅ Lombok `@Slf4j` 허용
- ❌ `System.out.println` 금지 → Logger 사용
- ✅ 생성자 또는 정적 팩토리(`of`, `from`, `create`) 우선
- ✅ 엔티티 기본 생성자는 `protected`, 생성 로직은 정적 팩토리
- ✅ Setter 대신 의도를 드러내는 변경 메서드 (`changePassword(...)`, `activate()`)

### DTO 전략 (Record + Static Factory) — CRITICAL

- DTO는 무조건 `record`
- 내부에 정적 팩토리(`from`/`of`) 필수
- 🚨 외부 `new DTO(...)` 직접 호출 금지 — 오직 `DTO.from(...)`/`DTO.of(...)` 만
- `from(...)`: 다른 객체 → DTO, `of(...)`: 원시값 → DTO
- DTO 네이밍: `CreateRequest`, `UpdateCommand`, `DetailResponse`, `ListQuery`
- DTO는 검증/매핑 외 비즈니스 로직 불포함

#### DTO 생성 규칙 가이드 (상세)

목적: DTO 생성 시점과 역할을 고정해, DTO 구조 변경 시 수정 범위를 DTO 내부로 한정한다.

핵심 규칙:

1. DTO는 `record`로 작성한다.
2. 외부에서는 생성자를 직접 호출하지 않는다.
3. `of(...)`는 원시값/직접 값 생성에만 사용한다.
4. `from(...)`은 다른 객체를 DTO로 변환할 때만 사용한다.
5. 변환 로직은 DTO 내부로 모은다.

예시:

```java
public record MemberSummaryResponse(Long id, String name) {
    public static MemberSummaryResponse of(Long id, String name) {
        return new MemberSummaryResponse(id, name);
    }

    public static MemberSummaryResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberSummaryResponse(member.getId(), member.getName());
    }
}
```

적용 팁:

1. 호출부에서는 `DTO.of(...)` 또는 `DTO.from(...)`만 사용한다.
2. DTO 구조 변경 시 DTO 내부에서만 수정되도록 유지한다.

계정 도메인 예시:

1. 직접 값 생성은 `of(...)`로 통일한다.
2. 엔티티/프로젝션 변환은 `from(...)`만 사용한다.

```java
AccountLoginIdQuery query = AccountLoginIdQuery.of(loginId);
LoginMemberResponse response = LoginMemberResponse.from(member);
LoginMemberResponse projectionResponse = LoginMemberResponse.from(view);
```

### 파라미터 전달 원칙 (DTO 우선) — CRITICAL

- 계층 경계에서 값을 개별 전달하지 말고 **DTO로 묶어 전달**
- 예외: `JpaRepository` 기본 메서드(`findById`, `save` 등)는 DTO 없이 사용
- Repository: `(검색조건 DTO 1개) + (Pageable 1개)` 패턴 표준

### 민감정보 마스킹

```java
public record LoginRequest(
        String loginId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {}
```

### JPA & Database

- Dirty Checking 우선, 불필요한 `repository.save()` 지양
- 연관관계 기본 `fetch = FetchType.LAZY` **명시**, `EAGER` 금지
- Cascade/orphanRemoval은 Aggregate Root에서만
- 컬럼/테이블 코멘트: `@Column(comment=...)` / `@Table(comment=...)`
- 단순/정적 조회는 파생 쿼리 우선, 동적/복잡 조회는 QueryDSL 강제
- Fetch Join으로 N+1 방지 (페이징 시 주의)
- 조회는 DTO Projection 우선
- 벌크 쿼리 시 `flush/clear` 고려
- Command: 엔티티 조회 우선 / Query: DTO 프로젝션 우선
- 총 건수 불필요하면 `Page` 대신 `Slice`
- `exists`/`count`는 전용 쿼리로 처리
- Enum 변경 시 DB 제약조건 동기화 + ALTER SQL 함께 제공

### QueryDSL Specification Pattern (권장)

- 서비스에서 where 절 나열 금지
- `BooleanExpression` 반환 정적 메서드로 정의, 조건 조립:

```java
where(MemberSpec.isActive(active), MemberSpec.hasRole(role), ...)
```

### 패키지 구조 (REST API 전용)

```
apps/{app}-api/src/main/java/com/example/{app}/
└── domain
    └── {app-specific-domain}
        └── api               # 앱 진입점/앱 전용 조합

libs/backend/global-core/src/main/java/com/example/global/
├── config
├── exception
├── security
└── utils

libs/backend/domain-core/src/main/java/com/example/domain/
└── {domain}
    ├── api                   # 🚨 /controller 경로 사용 금지
    ├── entity
    ├── enums
    ├── payload
    │   ├── request
    │   ├── response
    │   └── dto
    ├── repository
    ├── service
    │   ├── command
    │   └── query
    ├── validator
    ├── client
    │   └── payload
    ├── config
    └── support
```

#### 도메인별 특수 구조 (AI 참고용)

모든 도메인이 위 표준 레이아웃을 100% 따르지는 않는다.
아래 도메인은 역할 특성상 일부 패키지를 생략하며, 이는 **의도된 설계**이다.

| 도메인 | 특수 구조 | 사유 |
|--------|-----------|------|
| **account** | `entity`/`repository` 없음 | `AccountMemberQueryPort`를 통해 member 도메인에 위임하는 **조회·조합 전용 도메인** |
| **security** | `api`/`entity`/`repository` 없음 | JWT·Guard·블랙리스트 등 **횡단 관심사 도메인**, 자체 영속 엔티티 없음 |
| **social** | 루트에 `service` 없음, `google/` 서브도메인 중심 | 소셜 제공자별 서브도메인 구조(`social/google/service/`), 제공자 추가 시 동일 패턴 복제 |
| **aws** | `entity`/`repository`/`validator` 없음 | S3 파일 업로드 등 **외부 인프라 연동 전용 도메인** |
| **log** | 이벤트 리스너 + 관리자 조회 API | 활동 로그는 이벤트 리스너로 저장, 관리자 로그 조회용 `api/` 존재 |

### 멀티라인 문자열 (Text Block) — CRITICAL

- ❌ `"\n"` escape 금지
- ✅ Text Block(`""" ... """`) + `formatted(...)` 사용

---

## 3. 아키텍처 규칙 (Architecture Rules)

### CQRS (Command / Query 분리) — CRITICAL

- **CommandService**: 생성/수정/삭제, `@Transactional` 필수, 반환 `void` 또는 생성 ID
  - 예외: 인증 토큰 발급/이미지 업로드 URL 반환은 응답 DTO 허용
- **QueryService**: 조회, `@Transactional(readOnly = true)` 필수, DTO Projection 우선
- 패키지 분리: `service/command`, `service/query`
- 클래스명: `XxxCommandService` / `XxxQueryService`
- QueryService ↔ CommandService **상호 호출 금지**

### 전략 패턴 (Strategy Pattern)

- if-else/switch 타입 분기 금지
- `{Domain}StrategyFactory`로 구현체 분기
- 미등록 타입은 즉시 예외, 암묵적 기본값 금지

### Template Method + Resolver (권장)

- 흐름 동일 + 일부 정책만 다른 경우 Service 내부 if/else 금지
- 공통 흐름은 추상 클래스, 차이점은 Hook 메서드
- `@Transactional(AOP)` 주의: 공통 흐름 메서드를 `final`로 만들지 않기

### Hexagonal Architecture (Ports & Adapters) — CRITICAL

- 도메인 계층은 인프라(DB, 외부 API, 프레임워크)에 의존하지 않는다
- **Inbound Adapter**: 외부 요청을 도메인으로 연결 → `api/` 패키지 (Controller)
- **Outbound Port**: 도메인이 외부에 요청하는 인터페이스 → `support/` 패키지의 Port 인터페이스
- **Outbound Adapter**: Port 구현체로 인프라를 연결 → `support/` 패키지의 Adapter 구현체
- **의존 방향**: Adapter → Port ← Domain (**항상 안쪽으로**, 역방향 금지)
- 패키지 매핑:

| 패키지 | 헥사고날 역할 | 설명 |
|--------|-------------|------|
| `api/` | Inbound Adapter | Controller, 외부 요청 진입점 |
| `service/command/` | Application Service | 상태 변경 유스케이스 |
| `service/query/` | Application Service | 조회 유스케이스 |
| `entity/` | Domain Model | 핵심 비즈니스 모델 (Aggregate) |
| `support/` | Outbound Port + Adapter | 도메인 간 경계, 외부 인프라 추상화 |
| `repository/` | Outbound Adapter | 같은 도메인 내 JPA 영속화 |
| `client/` | Outbound Adapter | 외부 API 연동 |

### DDD Bounded Context 경계 — CRITICAL

- 도메인 간 참조는 **Port 인터페이스**(또는 이벤트/DTO/ID)로만 허용, **Repository·Entity·Service 직접 참조 금지**
- Port 인터페이스는 **사용하는(호출하는) 도메인**의 `support` 패키지에 정의
- Port 구현체(Adapter)는 **제공하는(구현하는) 도메인**의 `support` 패키지에 배치
- JPA 연관관계(`@ManyToOne` 등)로 인해 엔티티 참조가 불가피한 경우, Port 반환 타입에 엔티티를 허용하되 **주석으로 사유를 명시**
- Aggregate 내부 필드에 다른 도메인의 관심사(인증 토큰, 외부 연동 키 등)를 혼합하지 않는다
  - 불가피하게 같은 테이블에 저장해야 하면, 접근은 반드시 **해당 도메인의 Port를 경유**
- 기존 Port/Adapter 목록:

| Port (소비자 support/) | Adapter (제공자 support/) | 방향 |
|---|---|---|
| `AccountMemberQueryPort` | `AccountMemberQueryPortAdapter` | account → member |
| `AccountMemberCommandPort` | `AccountMemberCommandPortAdapter` | account → member |
| `AccountTokenRevocationPort` | `AccountTokenRevocationPortAdapter` | account → security |
| `AccountTokenRefreshPort` | `AccountTokenRefreshPortAdapter` | account → security |
| `AccountActivityPublishPort` | `AccountActivityPublishPortAdapter` | account → log |
| `MemberTokenRevocationPort` | `MemberTokenRevocationPortAdapter` | member → security |
| `MemberPermissionCheckPort` | `MemberPermissionCheckPortAdapter` | member → security |
| `MemberActivityPublishPort` | `MemberActivityPublishPortAdapter` | member → log |
| `SecurityMemberTokenPort` | `SecurityMemberTokenPortAdapter` | security → member |
| `SecurityMemberAccessPort` | `SecurityMemberAccessPortAdapter` | security → member |
| `SocialMemberRegistrationPort` | `SocialMemberRegistrationPortAdapter` | social → member |
| `SocialLoginTokenPort` | `SocialLoginTokenPortAdapter` | social → security |
| `SocialActivityPublishPort` | `SocialActivityPublishPortAdapter` | social → log |
| `MemberSocialCleanupPort` | `MemberSocialCleanupPortAdapter` | member → social |
| `InitMemberSeedPort` | `InitMemberSeedPortAdapter` | init → member |
| `LogAuthenticationCheckPort` | `LogAuthenticationCheckPortAdapter` | log → security |
| `MemberImageCommandPort` | `MemberImageCommandPortAdapter` | aws → member |
| `MemberImageStoragePort` | `S3MemberImageStoragePortAdapter` | member → aws |

#### Shared Kernel (도메인 간 공유 허용 타입)

아래 타입들은 **여러 Bounded Context에서 공통으로 사용되는 Shared Kernel**으로, 도메인 간 직접 참조를 허용한다.

| 타입 | 소속 도메인 | 공유 사유 |
|------|-------------|-----------|
| `AccountRole` | account/enums | 역할 기반 분기·검증에 전 도메인 필수 |
| `CurrentAccountDTO` | account/payload/dto | 인증 컨텍스트 전달에 security·member·log 등 필수 |
| `LogType` | log/enums | 활동 로그 발행 Port 파라미터로 account·member·social 사용 |
| `MemberActiveStatus` | member/enums | 회원 활성 상태 판별에 account·social 등 필수 |
| `MemberType` | member/enums | 회원 유형 분기에 account·social 등 필수 |
| `LoginTokenResponse` | account/payload/response | 로그인 토큰 반환에 security·social 필수 |
| `RefreshTokenResponse` | account/payload/response | 토큰 갱신 반환에 security 필수 |
| `AccountAuthMemberView` | account/payload/dto | 인증 주체 정보 전달에 security 필수 |
| `LoginMemberView` | account/payload/dto | 로그인 회원 뷰 전달에 security 필수 |

- Shared Kernel 타입은 Port 인터페이스 파라미터/반환 타입에 사용 가능
- Shared Kernel 이외의 타입(Service·Repository·Entity·내부 DTO)은 **반드시 Port 경유**

### AI 친화적 구조 (AI-Friendly Structure)

- **예측 가능한 네이밍**: 클래스명만으로 역할·계층·도메인을 파악할 수 있어야 한다
  - `{Domain}{역할}{계층}` 패턴: `MemberCommandService`, `SecurityMemberTokenPort`, `MemberSocialCleanupPortAdapter`
- **일관된 패키지 구조**: 모든 도메인이 동일한 패키지 레이아웃(`api/entity/enums/payload/repository/service/support`)을 따른다
- **자기 문서화 코드**: 주석보다 명확한 이름과 작은 메서드로 의도를 표현한다
- **단일 진입점**: 도메인 외부 접점은 Controller(인바운드) + Port(아웃바운드)로 한정한다
- **파일당 하나의 public 타입**: 검색·탐색·수정 범위를 최소화한다
- **작은 클래스, 작은 메서드**: AI가 컨텍스트 윈도우 내에서 전체를 파악할 수 있도록 한다
  - public 클래스 300라인, 메서드 30라인 초과 시 분리 고려

### 이벤트 기반 로깅

- ❌ `logRepository.save(...)` 직접 호출 금지
- ✅ `eventPublisher.publishEvent(new MemberActivityEvent(...))`
- 활동 로그: `AFTER_COMMIT`에서만 저장
- 예외/운영 로그: 커밋/롤백 모두 기록, `txStatus` 포함

---

## 4. REST API 규칙

### 응답 구조

- 성공: `{ "data": ... }`
- 에러: `{ "code": "...", "message": "...", "requestId": "..." }`
- 필드 검증 에러: `errors` 배열 추가 (권장)
- 응답 바디 불필요 시 `204 No Content`

### 컨트롤러 작성 원칙

- `RestApiController`로 응답 생성, 서비스에서 `ResponseEntity` 생성 금지
- 모든 API에 `@Operation(summary=...)` 작성
- Health 제외 모든 API에 `version = ApiVersioning.V1` 등 버전 매핑
- 상태 코드: POST→`201 Created`+Location, PUT/PATCH→`200`/`204`, DELETE→`204`

### API 설계 원칙

- URL은 **리소스 명사(복수형)** 중심, 동사 금지
- `GET`은 조회 전용, 요청 바디 금지 (예외: `/api/social/**` OAuth 콜백)
- `POST`=생성, `PUT`=전체 갱신(멱등), `PATCH`=부분 갱신, `DELETE`=삭제(멱등)
- 목록 조회: 쿼리 파라미터로 검색/필터/정렬/페이징
- 페이징: `page`는 1부터, `size` 최대치 제한 (`PaginationUtils` 정책)

### 예외 처리 / 검증 (CRITICAL)

- 전역 예외 처리: `@RestControllerAdvice`로 통일
- 에러 `code`는 **ErrorCode enum** 기준, 문자열 하드코딩 금지
- 검증 메시지는 **한국어**
- BindingResult 사용 지양, 검증 실패는 전역 예외 처리로 일원화

### InitBinder & ModelAttribute 규칙 (CRITICAL)

- ❌ 공용 이름(`form`/`dto`/`request`) 재사용 금지
- ✅ Request DTO 단위 1:1 매칭
- ✅ `addValidators(...)` 사용 (`setValidator(...)` 금지)
- ✅ 방어적 `supports(...)` 필수
- ❌ 컨트롤러에서 Validator 직접 호출 금지 — `@InitBinder` 등록 + `@Valid`/`@Validated` 자동 검증

### 검증 책임 분리

| 영역 | 대상 |
|------|------|
| Request DTO (Bean Validation) | `@NotBlank`, `@Min`, `@Email` 등 단순 필드 검증 |
| InitBinder Validator | 교차 필드, 조건부 필수값, 옵션 조합, 트리밍 |
| Service 계층 | DB/트랜잭션 상태 의존 검증 |

---

## 5. 보안 규칙

### 권한 통제는 PreAuthorize로만 (CRITICAL)

- 권한 필요 API에 `@PreAuthorize` 필수
- ❌ 서비스/컨트롤러 내부 if-else 권한 체크 금지
- 누락 = 공개 API로 간주
- 인증 필요 API에 `@SecurityRequirement(name = "Bearer Authentication")` 필수
- SpEL에서 패키지 의존형 `T(...)` 참조 지양 → `@Component` 메서드 호출로 캡슐화
- 인증/인가 체크는 **`MemberGuard`** `@Component`로 통합
- `SecurityUtils`/`SecurityContextHolder` 직접 호출 금지

### JWT/토큰 보안 규칙 (CRITICAL)

- 리프레시 토큰: **암호화 저장(AES-GCM 등)**, 복호화 검증 (해시 비교 금지)
- 사용자당 리프레시 토큰 **1개만 유효**
- 신규 발급 시 이전 토큰 즉시 폐기
- 복호화 실패/재사용 감지 시 토큰 전면 무효화
- 토큰 블랙리스트는 **해시 저장**
- 암호화/서명 키 회전 시 기존 토큰 전부 폐기
- API 보안 기본값: 인증 필요, 공개 API만 allowlist 명시

---

## 6. 품질 체크리스트

### 예외/Null/경계값

- [ ] NPE 가능성 없는가?
- [ ] 경계값(0, 음수, null, empty, max length) 처리되는가?

### 인증/인가

- [ ] `@PreAuthorize` 누락으로 공개되는 API 없는가?

### 토큰/세션

- [ ] 리프레시 토큰 암호화 저장 / 복호화 검증되는가?
- [ ] 재발급 시 이전 토큰 폐기되는가?

### API 응답/버전

- [ ] `RestApiController`로 응답 생성하는가?
- [ ] Health 제외 API에 `version = ApiVersioning.*` 명시되는가?
- [ ] Swagger 문서에서 `/api/health`, `/api/social/**` 제외 API의 `API-Version`이 `required=true`인가?

### 설정/운영 규칙

- [ ] 설정 변경 사유/영향 범위를 먼저 설명하고 확인받았는가?
- [ ] 문서/코드에서 실제 존재하지 않는 경로를 참조하지 않았는가?

### DTO 규칙

- [ ] DTO는 record인가?
- [ ] from/of 정적 팩토리 존재하는가?
- [ ] 외부에서 `new DTO(...)` 호출하지 않는가?

### DDD Bounded Context

- [ ] 다른 도메인의 Repository를 직접 주입/사용하지 않는가?
- [ ] 다른 도메인의 Service를 직접 호출하지 않고 Port/Event를 경유하는가?
- [ ] Aggregate에 다른 도메인의 관심사가 혼합되어 있지 않은가?

### Hexagonal Architecture

- [ ] 도메인 계층이 인프라(DB, 외부 API)에 직접 의존하지 않는가?
- [ ] 의존 방향이 항상 안쪽(Adapter → Port ← Domain)인가?
- [ ] 도메인 외부 접점이 Controller(인바운드) + Port(아웃바운드)로 한정되는가?

### AI 친화적 구조

- [ ] 클래스명만으로 역할·계층·도메인을 파악할 수 있는가?
- [ ] 모든 도메인이 동일한 패키지 레이아웃을 따르는가?
- [ ] public 클래스 300라인, 메서드 30라인 이내인가?

### Enum 계약 동기화

- [ ] API 계약 Enum(`com.example.domain.contract.enums.*`)과 대응 도메인 Enum의 `name()`이 동기화되어 있는가?
- [ ] Enum 변경 시 매핑(`toDomain()` / `fromDomain(...)`) 갱신이 반영되어 있는가?
- [ ] `pnpm nx test domain-core` 동기화 테스트를 통과했는가?

### Jackson 3

- [ ] import가 `tools.jackson.*`인가? (어노테이션은 `com.fasterxml.jackson.annotation.*`)

### JPA/성능

- [ ] 연관관계 `fetch = LAZY` 명시되는가?
- [ ] 엔티티에 `final` 남용 없는가? (프록시/Dirty Checking 방해 금지)

### 문자열/포맷

- [ ] `"\n"` 하드코딩 없는가?
- [ ] Text Block 사용하는가?

### Java 25 문법

- [ ] record/pattern matching/switch expression 우선 사용하는가?
- [ ] `var`이 가독성 해치지 않는가?
- [ ] Preview 기능 사용 시 옵션/영향 문서화되는가?

### Spring Framework 7

- [ ] `version = ApiVersioning.*` 선언되는가?
- [ ] `@HttpExchange` 우선 원칙 적용되는가?

### Spring Boot 4

- [ ] 신규 `RestTemplate` 도입 피했는가?
- [ ] 프로퍼티 rename 반영했는가?

---

## 7. 요약 (Cheatsheet)

| 구분            | 규칙                                                                             |
|---------------|--------------------------------------------------------------------------------|
| DTO           | record + `from/of`, 외부 `new` 금지                                                |
| 계층 경계         | 값 나열 금지, DTO 1개로 전달                                                            |
| 도메인 경계        | Port/Event/DTO/ID로만 참조, Repository·Entity·Service 직접 참조 금지                     |
| 헥사고날 아키텍처     | 의존 방향 항상 안쪽으로, Port/Adapter로 도메인-인프라 격리                                         |
| AI 친화적 구조     | 예측 가능한 네이밍(`{Domain}{역할}{계층}`), 일관된 패키지, 자기 문서화 코드                              |
| 스크립트          | `gradlew`/`gradlew.bat` 외 shell 스크립트 추가/수정은 사용자 요청 시만 진행                       |
| CQRS          | 물리 분리, Command=`@Transactional`, Query=`readOnly=true`                         |
| 조회 최적화        | QueryDSL + fetch join, DTO Projection                                          |
| 로깅            | traceId 포함, 민감정보 금지                                                            |
| API 버전        | `version = ApiVersioning.*`, 기본 `0.0`(무효), Swagger `API-Version required=true` |
| 컨트롤러          | `RestApiController` 응답, 서비스에서 `ResponseEntity` 금지                              |
| 설정 변경         | 설정 변경 사유/영향 범위를 먼저 설명하고 확인 후 진행                                                |
| Enum 계약 동기화   | `Api* == Domain name()` 유지 + `pnpm nx test domain-core` 통과                     |
| 외부 연동         | SDK → `@HttpExchange` → `@EnableHttpServices`                                  |
| 보안            | `@PreAuthorize`만, 누락=공개                                                        |
| 리프레시 토큰       | 암호화 저장 + 복호화 검증 + 재발급 시 폐기                                                     |
| JPA           | `LAZY` 명시, `EAGER` 금지                                                          |
| 멀티라인          | `"\n"` 금지, Text Block 사용                                                       |
| InitBinder    | DTO 1:1 매칭, 공용 이름 금지, `supports()` 방어                                          |
| 검증            | Bean Validation + InitBinder Validator, 서비스는 최종 보장만                            |
| Java 25       | record/pattern matching/switch 우선 + ScopedValue/Virtual Thread                 |
| Spring 7      | API Versioning + `@HttpExchange` 우선                                            |
| Spring Boot 4 | HTTP Service Clients/Virtual Thread/OpenTelemetry 우선                           |
| 버전 하한         | Java 25 + Boot 4 + Framework 7 미만 호환 타협 금지                                     |
| Jackson 3     | `tools.jackson.*`, 어노테이션만 `com.fasterxml.jackson.annotation.*`                 |
| Logback       | XML 설정 파일 미사용                                                                  |
