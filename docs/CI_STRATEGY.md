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

### main 브랜치 (예약)

현재 `main` 브랜치는 존재하지 않으며, `develop`이 기본 브랜치 역할을 한다.
실서버 운영이 시작되어 테스트/프로덕션 환경 분리가 필요해지면 `main`을 도입한다.

**도입 시점:** 실서버 릴리스 버전 관리가 필요할 때

**현재 구조 (테스트 서버만):**

```
feat/* ──→ develop ──→ deploy/user-api    (테스트 서버 배포)
          (Squash PR)  deploy/admin-api   (테스트 서버 배포)
```

**도입 후 구조 (테스트 + 실서버 분리):**

```
feat/* ──→ develop ──→ stage/user-api    (테스트 서버 배포)
          (Squash PR)  stage/admin-api   (테스트 서버 배포)
                │
                └──→ main ──→ deploy/user-api   (실서버 배포)
                    (PR)      deploy/admin-api  (실서버 배포)
                      │
                    태깅 (v1.0.0)
```

| 브랜치        | 환경         | 트리거            |
|------------|------------|----------------|
| `stage/*`  | 테스트 서버     | develop에서 push |
| `deploy/*` | 실서버 (프로덕션) | main에서 push    |

**GitHub 기본 브랜치:**

`main` 도입 후에도 **`develop`을 GitHub 기본(default) 브랜치로 유지**한다.
- PR 생성 시 자동으로 `develop`을 대상으로 지정 (가장 빈번한 워크플로우)
- `main`은 프로덕션 릴리스 전용 — `develop → main` PR은 릴리스 시에만 수동 생성
- Settings → General → Default Branch에서 변경하지 않음

**도입 절차:**

1. `develop`에서 `main` 브랜치 생성: `git checkout -b main develop && git push -u origin main`
2. `main`에 Branch Protection 설정 (develop과 동일 수준)
3. GitHub 기본 브랜치는 `develop` 유지 (변경하지 않음)
4. 기존 `deploy/*` 브랜치를 `stage/*`으로 이름 변경 (테스트 서버용)
5. 새 `deploy/*` 브랜치 생성 (실서버용, main 기반)
6. 배포 워크플로우 파일 추가: `stage-*.yml` (테스트), `deploy-*.yml` (실서버)
7. 릴리스 시 `develop → main` PR 생성 후 머지 → `main`에서 릴리스 태그 부여 (예: `v1.0.0`)

### feat 브랜치 네이밍 컨벤션

```
feat/be-xxx    # 백엔드 기능
feat/fe-xxx    # 프론트엔드 기능
feat/xxx       # 공통/인프라/문서
```

### 커밋 / PR 메시지 컨벤션

Squash merge를 사용하므로 **PR 제목 = develop에 남는 최종 커밋 메시지**이다.
개별 커밋은 작업 중 히스토리 용도이므로 간결해도 무방하지만, PR 제목과 본문은 구체적으로 작성한다.

**PR 제목** (= squash merge 커밋 제목):

```
<type>: <변경 요약>
```

| type       | 용도                   |
|------------|----------------------|
| `feat`     | 새로운 기능 추가            |
| `fix`      | 버그 수정                |
| `docs`     | 문서 변경                |
| `refactor` | 동작 변경 없는 코드 구조 개선    |
| `chore`    | 빌드/설정/CI 등 기능 외 변경   |
| `test`     | 테스트 추가/수정            |
| `rename`   | 이름 변경 (파일, 심볼 등)     |
| `style`    | 포맷팅, 세미콜론 등 코드 의미 무관 |

**PR 본문** (= squash merge 커밋 본문):

```markdown
## Summary
- 변경 사항 1 (무엇을 왜)
- 변경 사항 2
- 영향 범위 (어떤 파일/영역)

## Test plan
- [ ] 검증 항목 1
- [ ] 검증 항목 2
```

**예시:**

```
제목: feat: 소셜 로그인에 카카오 OAuth 추가

본문:
## Summary
- 카카오 OAuth2 인증 플로우 구현 (redirect → token → 회원 조회/생성)
- SocialType enum에 KAKAO 추가, KakaoOAuthClient 신규 생성
- application.yml에 카카오 설정 항목 추가

## Test plan
- [ ] 카카오 로그인 → 신규 회원 생성 확인
- [ ] 기존 회원 재로그인 시 토큰 정상 발급 확인
- [ ] CI 통과 확인
```

---

## CI 파이프라인

### 트리거 조건

```yaml
on:
  pull_request:
    branches: [develop]           # develop 대상 PR 생성/업데이트 시
  workflow_dispatch:              # GitHub UI에서 수동 실행
```

> **`push` 트리거를 제거한 이유:** Branch Protection의 "Require branches to be up to date" 옵션이 활성화되어 있어,
> PR CI는 항상 최신 develop 기반으로 실행된다. 따라서 머지 후 push CI 결과는 PR CI와 동일하며,
> 실패해도 이미 머지된 코드를 롤백하지 않으므로 실질적 의미가 없다. 제거로 CI 실행 횟수가 절반으로 줄어든다.

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
| **Require approvals**                                   | ✅ (0명) | PR 필수이나 리뷰 승인 없이 머지 가능                       |
| **Dismiss stale pull request approvals**                | ❌      | 새 커밋 push 시 기존 승인 취소 여부                      |
| **Require review from Code Owners**                     | ❌      | CODEOWNERS 기반 리뷰 강제 여부                       |
| **Require approval of the most recent reviewable push** | ❌      | 마지막 push에 대해 별도 승인 필요 여부                     |
| **Require status checks to pass**                       | ✅      | CI 통과 필수                                     |
| **Required checks**                                     | ✅      | `backend`, `frontend`, `e2e` 3개 job 모두 통과 필수 |
| **Require branches to be up to date**                   | ✅      | 최신 develop 기반으로 CI 통과 보장                     |
| **Require linear history**                              | ✅      | Squash merge 강제                              |
| **Do not allow bypassing**                              | ❌ (선택) | admin도 규칙 준수 강제                              |

### PR 충돌 및 통합 오류 방어

"Require branches to be up to date before merging" 옵션이 활성화되어 있어, 다른 PR이 먼저 머지된 경우 반드시 최신 develop 기반으로 업데이트한 뒤 CI를 재실행해야 머지할 수 있다.

**시나리오 1: 머지 충돌**

```
A: feat/a → develop PR (먼저 머지됨)
B: feat/b → develop PR (A와 같은 파일 수정 → 충돌)
```

1. GitHub가 "This branch has conflicts" 표시 → 머지 버튼 비활성화
2. B가 develop을 rebase/merge → 충돌 해결 → push
3. CI 재실행 → 통과 후 머지

**시나리오 2: 충돌은 없지만 합치면 에러**

```
A: feat/a → 함수 시그니처 변경 (먼저 머지됨)
B: feat/b → 기존 시그니처로 호출 (텍스트 충돌 없음, 빌드 에러)
```

1. A 머지 후 develop이 앞서감 → B는 "Update branch" 필요
2. B가 develop을 rebase/merge → CI 재실행 → 빌드 에러 발견
3. 에러 수정 → push → CI 통과 후 머지

> 이 옵션이 꺼져 있으면 오래된 브랜치가 그대로 머지되어 develop이 깨질 수 있다.
> 활성화 상태에서는 항상 최신 develop 기반으로 CI를 통과해야 하므로 통합 오류를 사전에 방지한다.

---

## 배포 전략

배포는 CI와 분리되어 있다. 배포 전용 브랜치에 push하면 해당 프로젝트가 자동 배포된다.

### 현재 구조 (테스트 서버만)

```
develop ──→ deploy/user-api   ──→ user-api 테스트 서버 배포
develop ──→ deploy/admin-api  ──→ admin-api 테스트 서버 배포
```

### main 도입 후 구조 (테스트 + 실서버)

```
develop ──→ stage/user-api   ──→ user-api 테스트 서버 배포
develop ──→ stage/admin-api  ──→ admin-api 테스트 서버 배포

main ──→ deploy/user-api   ──→ user-api 실서버 배포
main ──→ deploy/admin-api  ──→ admin-api 실서버 배포
```

### 배포 흐름 (현재 — 테스트 서버만)

```
개발자: git checkout deploy/user-api
        git merge develop
        git push
            │
            ▼
GitHub Actions: deploy-user-api.yml 트리거
    ├── checkout
    ├── ./gradlew :apps:user-api:bootJar
    ├── docker build → ghcr.io/.../user-api:<commit-sha>
    ├── docker push (GHCR)
    └── ssh ec2-user@테스트서버 "/opt/deploy/deploy.sh user-api <commit-sha>"
            │
테스트 서버 (EC2):
    ├── docker pull user-api:<commit-sha>
    ├── Green 컨테이너 기동 (8082)
    ├── health check → OK
    ├── Nginx upstream → Green(8082)
    ├── Blue 컨테이너 종료
    └── 완료 ✅
```

### 배포 흐름 (main 도입 후 — 테스트 + 실서버)

두 환경 모두 **동일한 Blue/Green 배포 과정**을 거친다. 차이는 소스 브랜치와 대상 서버뿐이다.

**테스트 서버 배포 (`stage/user-api` → EC2-A):**

```
개발자: git checkout stage/user-api
        git merge develop
        git push
            │
            ▼
GitHub Actions: stage-user-api.yml 트리거
    └── backend-cd.yml (재사용 워크플로우)
        ├── bootJar → Docker image → GHCR push
        └── ssh ec2-user@EC2-A → deploy.sh → Blue/Green 배포
```

**실서버 배포 (`deploy/user-api` → EC2-B):**

```
개발자: develop → main PR 생성 & 머지 (릴리스)
        git checkout deploy/user-api
        git merge main
        git push
            │
            ▼
GitHub Actions: deploy-user-api.yml 트리거
    └── backend-cd.yml (재사용 워크플로우)
        ├── bootJar → Docker image → GHCR push
        └── ssh ec2-user@EC2-B → deploy.sh → Blue/Green 배포
```

**환경별 차이:**

| 항목            | 테스트 서버                       | 실서버                    |
|---------------|------------------------------|------------------------|
| 브랜치           | `stage/user-api`             | `deploy/user-api`      |
| 소스            | `develop`에서 merge            | `main`에서 merge         |
| 워크플로우         | `stage-user-api.yml`         | `deploy-user-api.yml`  |
| 대상 서버         | EC2-A (테스트)                  | EC2-B (실서버)            |
| GitHub Secret | `STAGE_USER_API_SERVER_HOST` | `USER_API_SERVER_HOST` |

> 두 워크플로우 모두 같은 `backend-cd.yml`을 호출한다.
> `server-host` 파라미터(GitHub Secret)만 다르게 지정하면 된다.

### 배포 워크플로우의 동작 원리

각 프로젝트 워크플로우는 재사용 워크플로우(`backend-cd.yml`)를 호출하면서 **GitHub Secret으로 대상 서버를 결정**한다.

```yaml
# stage-user-api.yml (테스트 서버)
secrets:
  SERVER_HOST: ${{ secrets.STAGE_USER_API_SERVER_HOST }}  # → 테스트 서버 IP

# deploy-user-api.yml (실서버)
secrets:
  SERVER_HOST: ${{ secrets.USER_API_SERVER_HOST }}         # → 실서버 IP
```

`backend-cd.yml`은 전달받은 `SERVER_HOST`로 SSH 접속하여 배포한다:

```yaml
# backend-cd.yml (재사용 워크플로우)
- uses: appleboy/ssh-action@v1
  with:
    host: ${{ secrets.SERVER_HOST }}    # ← 전달받은 IP로 SSH 접속
    script: /opt/deploy/deploy.sh ...
```

**결과적으로 동일한 빌드+배포 로직이 Secret에 등록된 IP에 따라 다른 서버에 배포된다.**

### GitHub Secrets 등록 방법

Repository Settings → Secrets and variables → Actions → **Repository secrets**에 등록한다.
Name과 Secret을 입력하고 [Add secret]을 클릭한다. Secret은 1개씩 따로 등록한다.

**현재 (테스트 서버만):**

| Name                    | Secret (예시)             | 설명              |
|-------------------------|-------------------------|-----------------|
| `USER_API_SERVER_HOST`  | `10.0.1.10`             | user-api 서버 IP  |
| `ADMIN_API_SERVER_HOST` | `10.0.1.20`             | admin-api 서버 IP |
| `SERVER_USER`           | `ec2-user`              | SSH 접속 유저 (공통)  |
| `SSH_PRIVATE_KEY`       | EC2 키페어 `.pem` 파일 내용 전체 | SSH 키 (공통)      |

**main 도입 후 추가:**

| Name                          | Secret (예시) | 설명                  |
|-------------------------------|-------------|---------------------|
| `STAGE_USER_API_SERVER_HOST`  | `10.0.2.10` | user-api 테스트 서버 IP  |
| `STAGE_ADMIN_API_SERVER_HOST` | `10.0.2.20` | admin-api 테스트 서버 IP |

> main 도입 시 기존 `USER_API_SERVER_HOST`는 실서버 IP로 변경하고,
> 테스트 서버 IP는 `STAGE_*` Secret에 새로 등록한다.

### 배포 워크플로우 파일

**현재:**

| 파일                     | 트리거                      | 역할              |
|------------------------|--------------------------|-----------------|
| `deploy-user-api.yml`  | `push: deploy/user-api`  | user-api 배포 호출  |
| `deploy-admin-api.yml` | `push: deploy/admin-api` | admin-api 배포 호출 |
| `backend-cd.yml`       | `workflow_call` (재사용)    | 공통 빌드+배포 로직     |

**main 도입 후 추가:**

| 파일                    | 트리거                     | 역할                  |
|-----------------------|-------------------------|---------------------|
| `stage-user-api.yml`  | `push: stage/user-api`  | user-api 테스트 서버 배포  |
| `stage-admin-api.yml` | `push: stage/admin-api` | admin-api 테스트 서버 배포 |

> 기존 `deploy-*.yml`은 실서버 배포용으로 유지하고, `stage-*.yml`을 테스트 서버용으로 추가한다.

### 새 프로젝트 배포 추가 시

`deploy-user-api.yml`을 복사하여 `project`, `gradle-module`, 브랜치명만 변경하면 된다.
main 도입 후에는 `stage-*.yml`도 동일하게 추가한다.

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
