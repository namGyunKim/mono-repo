# Project Overview

## Purpose
모노레포 기반 풀스택 프로젝트. 백엔드(Spring Boot REST API)와 프론트엔드(Next.js)를 하나의 저장소에서 관리한다.

## Tech Stack

### Backend
- **Java 25** (Gradle Toolchain)
- **Spring Boot 4.0.3** / Spring Framework 7.x
- **Gradle 9.3.1** (Wrapper)
- **QueryDSL 7.1** (`io.github.openfeign.querydsl`)
- **PostgreSQL** (DB)
- **Jackson 3** (`tools.jackson.*` 패키지)
- **jjwt 0.13.0** (JWT)
- **AWS SDK v2** (S3)
- **SpringDoc OpenAPI 3.0.1** (Swagger)
- **Lombok** (`@Slf4j` 허용, `@Builder`/`@Setter`/`@Data` 금지)

### Frontend
- **Next.js 16** (App Router)
- **React 19**
- **TypeScript 5.9**

### Monorepo Tooling
- **Nx 22.x** + **pnpm 10.x**
- 패키지 매니저: `pnpm`

## Project Structure
```
mono-repo/
├── apps/
│   ├── user-api/          # Spring Boot (port 8081) — 사용자 전용 컨트롤러
│   ├── admin-api/         # Spring Boot (port 8082) — 관리자 전용 컨트롤러
│   └── web/               # Next.js 16
├── libs/
│   ├── backend/
│   │   ├── global-core/   # 전역 공통(도메인 비의존)
│   │   ├── domain-core/   # 도메인 핵심(account/member/log/social/aws)
│   │   ├── security-web/  # 인증/인가 웹 계층
│   │   └── web-support/   # MVC/AOP/예외/필터
│   └── shared/types/      # 공유 TS 타입
├── build.gradle.kts       # Gradle 루트
├── settings.gradle.kts    # Gradle 서브프로젝트
├── nx.json
├── package.json
└── docs/backend/          # 백엔드 가이드
    ├── README.md
    └── RULES.md
```

## Controller Isolation (CRITICAL)
- `apps/user-api`와 `apps/admin-api`는 각각 독립 컨트롤러 계층만 포함한다
- 비즈니스 로직(서비스/도메인/포트)은 `libs/backend/domain-core`에 집중한다
- 컨트롤러 앱에서 도메인 서비스를 직접 주입받되, 도메인 로직은 라이브러리에 위임한다

## Dependency Direction
`global-core <- domain-core <- security-web <- web-support <- apps/*-api`

## Domains in domain-core
- **account**: 계정 인증/로그인 (조회·조합 전용, 자체 entity/repository 없음)
- **member**: 회원 관리 (Strategy Pattern: User/Admin 분기)
- **log**: 활동 로그 (이벤트 리스너 기반)
- **social**: 소셜 로그인 (google 서브도메인)
- **aws**: S3 파일 업로드 (외부 인프라 연동)
- **security**: JWT/Guard/블랙리스트 (횡단 관심사)
- **init**: 초기 시드 데이터
- **contract/enums**: API 계약 Enum

## Test Infrastructure
- 순수 단위 테스트: JUnit5 + Mockito + AssertJ (`@SpringBootTest` 금지)
- 총 40개 파일, ~270개 테스트 메서드
- global-core: 26개 파일 (~185 메서드) — 유틸/보안/예외/AOP
- domain-core: 14개 파일 (~85 메서드) — Validator/서비스/Enum 동기화

## Key Reference Files
- `docs/backend/RULES.md` — 코딩 규칙 (최우선 기준)
- `docs/backend/README.md` — 구조/실행/운영 가이드
- `CLAUDE.md` — AI 에이전트 작업 지침
