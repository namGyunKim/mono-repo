# Code Style & Conventions

## 6 Core Principles
1. **SRP** — 클래스·메서드는 하나의 책임만
2. **Clean Code** — 읽기 쉬운 이름, 짧은 메서드
3. **CQRS** — Command/Query 물리 분리
4. **DDD** — Bounded Context, Aggregate, Port/Adapter
5. **Hexagonal Architecture** — 도메인을 인프라에서 격리
6. **AI 친화적 구조** — 예측 가능한 네이밍, 일관된 패키지

## Naming Conventions
- 클래스명: `{Domain}{역할}{계층}` 패턴 (e.g. `MemberCommandService`, `SecurityMemberTokenPort`)
- DTO: `CreateRequest`, `UpdateCommand`, `DetailResponse`, `ListQuery`
- 패키지: `/controller` 금지 → `/api` 사용

## DTO Rules (CRITICAL)
- DTO는 반드시 `record`
- 내부 정적 팩토리 `from()`/`of()` 필수
- 외부 `new DTO(...)` 직접 호출 금지
- `from()`: 다른 객체 → DTO, `of()`: 원시값 → DTO

## CQRS Rules
- `CommandService`: `@Transactional`, 반환 void/ID
- `QueryService`: `@Transactional(readOnly = true)`, DTO Projection
- 패키지: `service/command/`, `service/query/`
- 상호 호출 금지

## Java Style
- `final` 기본값 (재할당 불필요 변수, 파라미터, 지역변수 모두)
- Lombok: `@Slf4j` 허용, `@Builder`/`@Setter`/`@Data` 금지
- Text Block 사용 (`"\n"` 금지)
- record, pattern matching, switch expression 우선
- `System.out.println` 금지 → Logger
- 메서드 30라인, 클래스 300라인 초과 시 분리 고려
- 중첩 깊이 2단계 이내

## Test Code Conventions (CRITICAL)
- **순수 단위 테스트**: Spring Context 로딩 없음 (`@SpringBootTest` 금지)
- **테스트 스택**: JUnit5 + Mockito (`@ExtendWith(MockitoExtension.class)`) + AssertJ (`assertThat`)
- **클래스 접근 제한**: package-private (public 금지)
- **메서드 네이밍**: `{메서드}_{시나리오}_{기대결과}` (e.g. `encrypt_null_returns_empty`)
- **패턴**: AAA (Arrange-Act-Assert), 메서드당 하나의 동작 검증
- **Mockito 규칙**:
  - `@Mock` 필드로 의존성 선언
  - `@InjectMocks`로 테스트 대상 자동 주입
  - `when().thenReturn()` 스텁, `verify()` 검증
  - 불필요한 스텁은 삭제 (StrictStubs 위반 방지)
- **final 필수**: 테스트 메서드 내 지역변수도 `final` 적용

## JPA Rules
- `fetch = FetchType.LAZY` 명시, EAGER 금지
- Dirty Checking 우선
- 엔티티 기본 생성자 protected
- Command=엔티티 조회 우선, Query=DTO Projection 우선
- QueryDSL Specification Pattern 권장

## Jackson 3
- `tools.jackson.*` (core/databind)
- `com.fasterxml.jackson.annotation.*` (annotations만)

## Security
- `@PreAuthorize` 필수, 누락=공개 API
- `MemberGuard` @Component로 인증/인가 통합
- Refresh Token: 암호화 저장 + 복호화 검증

## API Rules
- `RestApiController`로 응답 생성
- `version = ApiVersioning.V1` 필수 (health/social 제외)
- URL 버저닝 금지
- 커밋 메시지: `type(scope): 한국어 요약` (한 줄)

## Bounded Context
- 도메인 간 참조: Port 인터페이스로만
- Port는 소비자 도메인의 `support/`에 정의
- Adapter는 제공자 도메인의 `support/`에 배치
- Shared Kernel 타입(AccountRole, CurrentAccountDTO 등)만 직접 참조 허용

## Language
- 모든 답변/주석/커밋 메시지: 한국어
