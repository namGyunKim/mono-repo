# mono-repo

Nx + Gradle 기반 모노레포입니다.  
현재 구조는 `모노레포 + MSA 확장형`을 기준으로 정리되어 있으며, 백엔드 공통 코드는 `libs/backend`에 집중하고 앱(`apps/*`)은 API/유스케이스 조합 책임에 집중합니다.

## 프로젝트 현황

- 형태: 모노레포 + 서비스 분리(MSA) 확장형
- 백엔드 기준: Java 25, Spring Boot 4.0.3, Spring Framework 7
- 프론트엔드 기준: Next.js 16, React 19
- 워크스페이스 툴링: Nx 22, pnpm 10, Gradle 9.3.1(Wrapper)

## 워크스페이스 구조

```text
mono-repo/
├── apps/
│   ├── user-api/               # Spring Boot 사용자 API (port: 8081)
│   ├── admin-api/              # Spring Boot 관리자 API (port: 8082)
│   └── web/                    # Next.js 프론트엔드
├── libs/
│   ├── backend/
│   │   ├── global-core/        # 전역 공통(도메인 비의존)
│   │   ├── domain-core/        # 도메인 핵심(account/member/log/social 등)
│   │   ├── security-web/       # Spring Security 웹 계층 어댑터
│   │   └── web-support/        # MVC/AOP/예외 처리/웹 공통 지원
│   └── shared/
│       └── types/              # 프론트/백엔드 공유 TS 타입
├── docs/
│   └── backend/
├── build.gradle.kts
├── settings.gradle.kts
└── nx.json
```

## 모듈 책임과 의존 방향

### apps

- `apps/user-api`, `apps/admin-api`: 앱 전용 API, 앱 유스케이스 조합, 앱별 엔드포인트(예: health)
- `apps/web`: 프론트엔드 애플리케이션

### libs/backend

- `global-core`: 여러 앱에서 공통으로 재사용되는 최소 전역 컴포넌트
- `domain-core`: 도메인 모델/서비스/도메인 규칙
- `security-web`: 인증/인가 웹 어댑터(필터, 핸들러, 보안 설정)
- `web-support`: 웹 계층 공통 지원(MVC/AOP/예외/버전 헤더 처리 등)

### libs/shared

- `types`: 프론트/백엔드가 함께 쓰는 TypeScript 타입

### 백엔드 의존 흐름(개념)

`global-core <- domain-core <- security-web <- web-support <- apps/*-api`

## 시작하기

### 요구사항

- JDK 25
- Node.js 23.x
- pnpm 10.x

### 설치

```bash
pnpm install
```

### 프로젝트 목록 확인

```bash
pnpm nx show projects
```

## 실행/빌드 명령

### 백엔드 (Nx 경유)

```bash
pnpm nx serve user-api
pnpm nx serve admin-api

pnpm nx build user-api
pnpm nx build admin-api

pnpm nx test user-api
pnpm nx test admin-api
```

### 백엔드 (Gradle 직접)

```bash
./gradlew :apps:user-api:bootRun
./gradlew :apps:admin-api:bootRun

./gradlew :apps:user-api:build
./gradlew :apps:admin-api:build
```

### 프론트엔드

```bash
pnpm nx dev @mono-repo/web
pnpm nx build @mono-repo/web
pnpm nx start @mono-repo/web
```

## 개발 운영 원칙

- 재사용 가능한 백엔드 코드는 `libs/backend/*`에 우선 배치합니다.
- 앱(`apps/*-api`)은 앱 고유 정책, API 조합, 배포 단위 책임에 집중합니다.
- 도메인 전용 모델/Enum/응답 타입은 전역(`global`)이 아닌 해당 도메인 경계에 둡니다.
- 서비스 분리(MSA) 시 `apps/*-api`를 기준으로 점진 분리 가능한 구조를 유지합니다.

## 문서

- 백엔드 개요: `docs/backend/README.md`
- 백엔드 규칙: `docs/backend/RULES.md`
