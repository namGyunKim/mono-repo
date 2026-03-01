# CI/CD 전략

## 브랜치 전략

```
feat/*  ──→  develop  ──→  deploy/*
          (Squash PR)    (merge/push)
```

| 브랜치                | 역할                                               | 보호 규칙                |
|--------------------|--------------------------------------------------|----------------------|
| `feat/*`           | 기능 개발 (예: `feat/be-member-api`, `feat/fe-login`) | 없음                   |
| `develop`          | 통합 브랜치 — 모든 feat이 여기로 머지됨                        | Branch Protection 적용 |
| `deploy/user-api`  | user-api 배포 트리거                                  | push 시 자동 배포         |
| `deploy/admin-api` | admin-api 배포 트리거                                 | push 시 자동 배포         |

### feat 브랜치 네이밍 컨벤션

```
feat/be-xxx    # 백엔드 기능
feat/fe-xxx    # 프론트엔드 기능
feat/xxx       # 공통/인프라/문서
```

---

## CI 파이프라인

### 트리거 조건

```yaml
on:
  push:
    branches: [develop]           # develop에 직접 push 시
  pull_request:
    branches: [develop]           # develop 대상 PR 생성/업데이트 시
  workflow_dispatch:              # GitHub UI에서 수동 실행
```

### Job 구조

```
feat/* → develop PR 생성 시:

┌────────────────────┐  ┌─────────────────────┐
│  backend (40s)     │  │  frontend (28s)      │
│  Gradle build      │  │  lint + test + build │
└────────┬───────────┘  └──────────┬──────────┘
         └──────────┬──────────────┘
                    ▼
         ┌─────────────────────┐
         │  e2e (2m~)          │
         │  Playwright 통합    │
         │  테스트              │
         └─────────────────────┘
                    ▼
              ✅ 전부 통과 시
           Squash Merge 가능
```

---

## Job 상세

### 1. backend

백엔드 컴파일 및 단위 테스트를 실행한다.

| 항목     | 값                                                      |
|--------|--------------------------------------------------------|
| Runner | `ubuntu-latest`                                        |
| Java   | Temurin 21 (Gradle toolchain이 25로 자동 프로비저닝)            |
| 명령어    | `./gradlew :apps:admin-api:build :apps:user-api:build` |
| 포함 범위  | 컴파일, 단위 테스트 (~270개), QueryDSL 코드 생성                    |
| 평균 소요  | ~40초                                                   |

### 2. frontend

프론트엔드 코드 품질 및 빌드를 검증한다.

| 항목      | 값                                                               |
|---------|-----------------------------------------------------------------|
| Runner  | `ubuntu-latest`                                                 |
| Node    | 20                                                              |
| 패키지 매니저 | pnpm (frozen-lockfile)                                          |
| 명령어     | `pnpm nx run-many -t lint test build --projects='@mono-repo/*'` |
| 포함 범위   | ESLint, 단위 테스트, Next.js 프로덕션 빌드                                 |
| 평균 소요   | ~28초                                                            |

### 3. e2e

프론트엔드 + 백엔드 통합 E2E 테스트를 실행한다. backend, frontend job이 **모두 통과한 후**에만 실행된다.

| 항목       | 값                                      |
|----------|----------------------------------------|
| Runner   | `ubuntu-latest`                        |
| 의존       | `needs: [backend, frontend]`           |
| DB       | PostgreSQL 16 서비스 컨테이너 (`postgres:16`) |
| 브라우저     | Chromium (Playwright)                  |
| 테스트 프로젝트 | `apps/web-e2e/`                        |
| 명령어      | `pnpm nx e2e web-e2e`                  |
| 평균 소요    | ~2분                                    |

**E2E job 실행 순서:**

1. Java 21 + Node 20 + pnpm 설치
2. Playwright Chromium 브라우저 설치
3. 백엔드 설정 파일 생성 (application.yml, application-ci.yml 인라인 생성)
4. `./gradlew :apps:user-api:bootRun &` (백그라운드)
5. `pnpm nx build @mono-repo/web` → `pnpm nx start @mono-repo/web &`
6. 헬스체크 대기 (user-api `/api/health`, Next.js `localhost:3000`)
7. Playwright E2E 테스트 실행
8. 테스트 리포트 아티팩트 업로드

**E2E 테스트 목록:**

| 파일                  | 테스트 내용                         |
|---------------------|--------------------------------|
| `health.spec.ts`    | 백엔드 `GET /api/health` 200 응답   |
| `home.spec.ts`      | 홈페이지 렌더링, Welcome 텍스트 확인       |
| `api-hello.spec.ts` | Next.js `GET /api/hello` 응답 확인 |

**E2E Playwright 설정 (`apps/web-e2e/playwright.config.ts`):**

| 옵션           | 로컬                     | CI                        |
|--------------|------------------------|---------------------------|
| `retries`    | 0                      | 2 (flaky 테스트 방어)          |
| `workers`    | auto                   | 1 (러너 리소스 제한)             |
| `trace`      | retain-on-failure      | on-first-retry (디버깅용)     |
| `screenshot` | only-on-failure        | only-on-failure           |
| `reporter`   | html (on-failure 시 열기) | html + github (PR 인라인 주석) |
| `webServer`  | Next.js 자동 기동          | 비활성 (CI에서 수동 관리)          |
| `forbidOnly` | false                  | true (`.only` 실수 방지)      |

---

## Branch Protection 설정 (develop)

GitHub Settings → Branches → Branch protection rules → `develop`:

| 옵션                                                      | 현재 설정  | 설명                                           |
|---------------------------------------------------------|--------|----------------------------------------------|
| **Require a pull request before merging**               | ✅      | 직접 push 차단, PR 필수                            |
| **Require approvals**                                   | ✅ (1명) | 최소 1명의 리뷰 승인 필요                              |
| **Dismiss stale pull request approvals**                | ❌      | 새 커밋 push 시 기존 승인 취소 여부                      |
| **Require review from Code Owners**                     | ❌      | CODEOWNERS 기반 리뷰 강제 여부                       |
| **Require approval of the most recent reviewable push** | ❌      | 마지막 push에 대해 별도 승인 필요 여부                     |
| **Require status checks to pass**                       | ✅      | CI 통과 필수                                     |
| **Required checks**                                     | ✅      | `backend`, `frontend`, `e2e` 3개 job 모두 통과 필수 |
| **Require branches to be up to date**                   | ✅      | 최신 develop 기반으로 CI 통과 보장                     |
| **Require linear history**                              | ✅      | Squash merge 강제                              |
| **Do not allow bypassing**                              | ❌ (선택) | admin도 규칙 준수 강제                              |

---

## 배포 전략

배포는 CI와 분리되어 있다. `deploy/*` 브랜치에 push하면 해당 프로젝트가 자동 배포된다.

```
develop ──→ deploy/user-api   ──→ user-api 서버 배포
develop ──→ deploy/admin-api  ──→ admin-api 서버 배포
```

### 배포 흐름

```
1. develop에서 deploy/* 브랜치로 merge
   $ git checkout deploy/user-api
   $ git merge develop
   $ git push

2. GitHub Actions 자동 실행
   bootJar → Docker 이미지 빌드 → GHCR push → SSH 배포

3. 서버에서 Blue/Green 무중단 배포
   deploy.sh → 새 컨테이너 기동 → 헬스체크 → Nginx 전환
```

### 배포 워크플로우 파일

| 파일                     | 트리거                      | 역할              |
|------------------------|--------------------------|-----------------|
| `deploy-user-api.yml`  | `push: deploy/user-api`  | user-api 배포 호출  |
| `deploy-admin-api.yml` | `push: deploy/admin-api` | admin-api 배포 호출 |
| `deploy-backend.yml`   | `workflow_call` (재사용)    | 공통 빌드+배포 로직     |

### 새 프로젝트 배포 추가 시

`deploy-user-api.yml`을 복사하여 `project`, `gradle-module`, 브랜치명만 변경하면 된다.

---

## 로컬 개발 워크플로우

### 백엔드 단위 테스트
```bash
./gradlew test                     # 전체
pnpm nx test user-api              # user-api만
pnpm nx test admin-api             # admin-api만
```

### 프론트엔드
```bash
pnpm nx dev @mono-repo/web         # 개발 서버
pnpm nx build @mono-repo/web       # 프로덕션 빌드
pnpm nx lint @mono-repo/web        # 린트
```

### E2E 테스트 (로컬)
```bash
# 터미널 1: 백엔드 (PostgreSQL 실행 필요)
./gradlew :apps:user-api:bootRun

# 터미널 2: E2E (Next.js는 자동 기동)
pnpm nx e2e web-e2e
```

---

## CI 아티팩트

E2E 실패 시 GitHub Actions에서 아티팩트를 다운로드할 수 있다.

| 아티팩트                      | 조건    | 보존 기간 | 내용                   |
|---------------------------|-------|-------|----------------------|
| `playwright-report`       | 항상    | 14일   | HTML 리포트 (테스트 결과 전체) |
| `playwright-test-results` | 실패 시만 | 7일    | 스크린샷, trace 파일       |
