# 백엔드 개발 가이드

이 문서는 모노레포의 백엔드 영역(`apps/*-api`, `libs/backend/*`)에 대한 **현재 구조/실행 방법/운영 기준**을 설명합니다.

코딩 규칙(아키텍처/컨벤션/보안)은 [RULES.md](./RULES.md)를 기준으로 합니다.

---

## 1. 백엔드 범위

- 애플리케이션
  - `apps/user-api` (포트 `8081`)
  - `apps/admin-api` (포트 `8082`)
- 공통 라이브러리
  - `libs/backend/global-core`
  - `libs/backend/domain-core`
  - `libs/backend/security-web`
  - `libs/backend/web-support`

---

## 2. 실제 워크스페이스 구조

```text
mono-repo/
├── apps/
│   ├── user-api/                 # 사용자 API 애플리케이션
│   └── admin-api/                # 관리자 API 애플리케이션
├── libs/
│   └── backend/
│       ├── global-core/          # 전역 공통(도메인 비의존)
│       ├── domain-core/          # 도메인 핵심(account/member/log/social 등)
│       ├── security-web/         # 인증/인가 웹 계층 어댑터
│       └── web-support/          # MVC/AOP/예외/API-Version 필터 등
├── build.gradle.kts
├── settings.gradle.kts
└── docs/backend/
```

의존 방향(개념):

`global-core <- domain-core <- security-web <- web-support <- apps/*-api`

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

```bash
pnpm nx show projects

pnpm nx serve user-api
pnpm nx serve admin-api

pnpm nx build user-api
pnpm nx build admin-api

pnpm nx test user-api
pnpm nx test admin-api
```

### Gradle 직접

```bash
./gradlew :apps:user-api:bootRun
./gradlew :apps:admin-api:bootRun

./gradlew :apps:user-api:build
./gradlew :apps:admin-api:build
```

---

## 5. 빠른 확인 URL

### user-api (`localhost:8081`)

- `GET /` : 서버 안내
- `GET /api/health` : 헬스체크
- `GET /swagger-ui.html` : Swagger UI (활성화 환경에서만)

### admin-api (`localhost:8082`)

- `GET /` : 서버 안내
- `GET /api/health` : 헬스체크
- `GET /swagger-ui.html` : Swagger UI (활성화 환경에서만)

---

## 6. API 버저닝 규칙 요약

- 기본 정책: `/api/**` 요청은 `API-Version` 헤더 필수
- 예외: `/api/health`, `/api/social/**`
- URL 버저닝(`/v1`, `/api/v1`)은 사용하지 않음

상세 규칙과 예외 처리 원칙은 [RULES.md](./RULES.md)의 CRITICAL 규칙을 따릅니다.

---

## 7. 문서 역할 분리

- 이 문서: 구조/실행/운영 기준(개요)
- [RULES.md](./RULES.md): 코드 작성 및 리뷰 시 반드시 지켜야 하는 세부 규칙

충돌 시 최신 정책은 `RULES.md`를 우선 기준으로 하고, 필요한 경우 두 문서를 함께 갱신합니다.
