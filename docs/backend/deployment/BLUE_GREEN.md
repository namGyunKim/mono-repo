# Blue/Green 무중단 배포 가이드

ALB + EC2 2대에서 Docker + Nginx + GitHub Actions를 이용한 Blue/Green 무중단 배포 전략.
EC2마다 1개의 프로젝트를 운영하며, 대상 프로젝트만 바꾸면 동일한 구조를 그대로 재사용할 수 있다.

---

## 로컬(내 컴퓨터) 세팅

서버를 세팅하기 전에 내 컴퓨터에서 먼저 준비해야 하는 항목들이다.

```bash
# 1. GitHub Secrets 등록 (GitHub Actions가 서버에 SSH 접속하기 위해 필요)
#    리포지토리 Settings → Secrets and variables → Actions
#    ┌────────────────────────┬──────────────────────────────────────────┐
#    │ Secret                 │ 값                                       │
#    ├────────────────────────┼──────────────────────────────────────────┤
#    │ USER_API_SERVER_HOST   │ user-api EC2 IP (예: 10.0.1.10)         │
#    │ ADMIN_API_SERVER_HOST  │ admin-api EC2 IP (예: 10.0.1.20)        │
#    │                        │ 2대 이상: 쉼표로 구분 (예: 10.0.1.10,10.0.1.11) │
#    │                        │ → 첫 번째 서버 배포 완료 후 두 번째 서버 순차 배포  │
#    │ SERVER_USER            │ EC2 기본 유저 (예: ec2-user)              │
#    │ SSH_PRIVATE_KEY        │ EC2 키페어 .pem 파일 내용 전체             │
#    └────────────────────────┴──────────────────────────────────────────┘
#    → SSH_PRIVATE_KEY에는 EC2 인스턴스 생성 시 발급받은 .pem 파일 내용을 그대로 붙여넣는다.
#      cat ~/.ssh/my-key.pem  →  전체 복사 후 Secret value에 입력
#
#    참고: GHCR push는 워크플로우의 GITHUB_TOKEN이 자동 처리하므로 별도 설정 불필요.

# 2. GHCR PAT 발급 (서버에서 이미지 pull 시 사용 — GitHub Actions와는 무관)
#    GitHub → Settings → Developer settings → Personal access tokens
#    → Generate new token → 스코프: read:packages
#    → 발급된 토큰을 메모 (서버 세팅 3단계 docker login에서 입력)

# 3. 배포 브랜치 생성 (최초 1회)
git push origin main:deploy/user-api
git push origin main:deploy/admin-api
#    → 각 프로젝트의 GitHub Actions 워크플로우가
#      on.push.branches에 해당 브랜치를 트리거로 등록하고 있다.
#      따라서 deploy/user-api 브랜치에 push하면 워크플로우가 자동 실행되어
#      JAR 빌드 → Docker 이미지 → GHCR push → SSH 배포까지 진행된다.
#
#      워크플로우 파일 위치:
#        원본: docs/backend/deployment/user-api/deploy-user-api.yml
#        실제: .github/workflows/deploy-user-api.yml (GitHub Actions가 인식하는 경로)
```

끝. 이제 아래 서버 세팅을 진행한다.

---

## 서버 세팅 (user-api 기준)

서버를 새로 만들면 아래 9단계를 순서대로 실행한다. 10분이면 끝난다.

```bash
# 1. Docker 설치
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
newgrp docker

# 2. Nginx + cronie 설치
sudo dnf install -y nginx cronie
sudo systemctl enable --now nginx crond

# 3. GHCR 로그인 (이미지 pull 용)
echo "<GHCR_PAT>" | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin

# 4. 디렉토리 생성
sudo mkdir -p /opt/deploy/{projects,nginx/conf.d}
sudo chown -R $USER:$USER /opt/deploy
sudo mkdir -p /app/{logs,backup}
sudo chown -R $USER:$USER /app

# 5. 파일 복사 (docs/backend/deployment/ 에서)
cp deploy.sh /opt/deploy/deploy.sh && chmod +x /opt/deploy/deploy.sh
cp user-api/user-api.env /opt/deploy/projects/
cp user-api/user-api-upstream.conf /opt/deploy/nginx/conf.d/
sudo cp user-api/user-api.conf /etc/nginx/conf.d/

# 6. 서버별 값 수정
#    /opt/deploy/projects/user-api.env → IMAGE, SPRING_PROFILES
#    /etc/nginx/conf.d/user-api.conf   → server_name

# 7. Nginx에 upstream 디렉토리 연결 (최초 1회)
sudo bash -c 'grep -q "/opt/deploy/nginx" /etc/nginx/nginx.conf || \
  sed -i "/http {/a\\    include /opt/deploy/nginx/conf.d/*.conf;" /etc/nginx/nginx.conf'

# 8. Nginx 검증 & 적용
sudo nginx -t && sudo nginx -s reload

# 9. 로그 백업 cron 등록
#    매일 새벽 2시: /app/logs의 롤링 로그(1일 이상 경과)를 gzip 압축 후 /app/backup으로 이동
#    매일 새벽 3시: /app/backup에서 90일 넘은 백업 파일 자동 삭제
#    → /app/logs에는 최근 로그만, /app/backup에는 최대 90일치 압축 백업이 유지된다
(crontab -l 2>/dev/null; echo '0 2 * * * find /app/logs -name "app-*.log" -mtime +1 -exec gzip {} \; -exec mv {}.gz /app/backup/ \;') | crontab -
(crontab -l 2>/dev/null; echo '0 3 * * * find /app/backup -name "*.gz" -mtime +90 -delete') | crontab -
```

끝. 이후 `deploy/user-api` 브랜치에 push하면 자동 배포된다.

---

## 빠른 요약 — 서버에 필요한 것

user-api 서버 세팅 기준. 다른 프로젝트는 해당 프로젝트 디렉토리의 파일을 사용한다.

### 파일

| 서버 경로 | 원본 | 비고 |
|-----------|------|------|
| `/opt/deploy/deploy.sh` | [`deploy.sh`](deploy.sh) | 모든 서버 동일 |
| `/opt/deploy/projects/user-api.env` | [`user-api/user-api.env`](user-api/user-api.env) | `IMAGE`, `SPRING_PROFILES`, 로깅 설정 |
| `/opt/deploy/nginx/conf.d/user-api-upstream.conf` | [`user-api/user-api-upstream.conf`](user-api/user-api-upstream.conf) | deploy.sh가 자동 관리 |
| `/etc/nginx/conf.d/user-api.conf` | [`user-api/user-api.conf`](user-api/user-api.conf) | `server_name` 수정 |

### 서버마다 달라지는 값

| 항목 | 파일 | 설명 |
|------|------|------|
| `IMAGE` | `.env` | GHCR 이미지 경로의 OWNER 부분 |
| `SPRING_PROFILES` | `.env` | `prod`, `staging` 등 |
| `server_name` | Nginx conf | 실제 도메인 |

> `deploy.sh`, 포트(8081/8082), health check 경로, 디렉토리 구조는 모든 서버에서 동일하다.

### 프로젝트별 파일

| 프로젝트 | 디렉토리 |
|----------|----------|
| user-api | [`docs/backend/deployment/user-api/`](user-api/) |
| admin-api | [`docs/backend/deployment/admin-api/`](admin-api/) |

---

## 1. 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│  GitHub                                                 │
│  push → Actions → build JAR → Docker image → Push GHCR  │
└──────────────┬──────────────────────┬───────────────────┘
               │ SSH                  │ SSH
┌──────────────▼───────┐  ┌──────────▼────────────┐
│  ALB (HTTPS:443)     │  │                       │
│  + ACM SSL + WAF     │  │                       │
└──────┬─────────┬─────┘  │                       │
       │         │        │                       │
┌──────▼──┐  ┌───▼─────┐  │                       │
│ EC2-1   │  │ EC2-2   │  │                       │
│ Nginx   │  │ Nginx   │  │                       │
│ (:80)   │  │ (:80)   │  │                       │
│ ┌─────┐ │  │ ┌─────┐ │  │                       │
│ │Blue │ │  │ │Blue │ │  │                       │
│ │8081 │ │  │ │8081 │ │  │                       │
│ ├─────┤ │  │ ├─────┤ │  │                       │
│ │Green│ │  │ │Green│ │  │                       │
│ │8082 │ │  │ │8082 │ │  │                       │
│ └─────┘ │  │ └─────┘ │  │                       │
│user-api │  │admin-api│  │                       │
└────┬────┘  └────┬────┘  │                       │
     │            │       │                       │
  ┌──▼────────────▼──┐    │                       │
  │  DB 서버          │    │                       │
  │  PostgreSQL      │    │                       │
  └──────────────────┘    │                       │
                          └───────────────────────┘
```

> ALB가 HTTPS 종료 + WAF를 처리하므로, EC2의 Nginx는 HTTP(:80)만 수신한다.
> DB는 별도 서버에 분리한다. 앱 서버에는 Docker + Nginx만 존재한다.

### 배포 흐름 (deploy.sh)

블루가 떠있는 상태에서 새 배포가 시작되면:

```
 현재 상태                        배포 중                          완료
 ─────────                      ──────                         ────
 Nginx → Blue(8081) ✅           Nginx → Blue(8081) ✅           Nginx → Green(8082) ✅
         Green ❌                        Green(8082) 기동 중             Blue 종료
```

| 단계 | 동작 | 실패 시 |
|------|------|---------|
| 1 | 새 이미지 pull | 중단, 기존 컨테이너 유지 |
| 2 | 그린(8082) 컨테이너 기동 | 중단, 기존 컨테이너 유지 |
| 3 | 그린 health check (`/api/health`) | 그린 제거, 블루 그대로 유지 |
| 4 | Nginx upstream을 그린(8082)으로 전환 + reload | 그린 제거, 블루 그대로 유지 |
| 5 | 블루 컨테이너 graceful shutdown (30초 대기) | — |
| 6 | 활성 컬러를 `green`으로 기록 | — |

> 다음 배포에서는 방향이 반대가 된다: 그린(8082) 유지 → 블루(8081) 기동 → 전환.
> 어느 단계에서 실패하더라도 기존 컨테이너가 그대로 트래픽을 처리하므로 서비스 중단이 없다.

### 핵심 원칙

| 원칙 | 설명 |
|------|------|
| **빌드/배포 분리** | CI에서 빌드, 서버에서는 pull + 기동만 |
| **불변 아티팩트** | Docker 이미지 = 배포 단위, 서버에 소스코드 없음 |
| **포트 컨벤션** | 모든 서버 동일: Blue=8081, Green=8082 |
| **1서버 1프로젝트** | 서버마다 하나의 프로젝트만 운영한다 |
| **재사용** | 프로젝트명만 바꾸면 동일 구조 적용 |

---

## 2. 사전 준비

### 2.1 Health Check 엔드포인트

배포 스크립트가 새 컨테이너의 정상 기동 여부를 판단하기 위해 health check 엔드포인트가 필요하다.

현재 각 프로젝트에 `/api/health` 엔드포인트가 이미 존재한다 (`HealthRestController`).
배포 스크립트는 이 엔드포인트를 사용한다.

### 2.2 GitHub Secrets 등록

리포지토리 Settings → Secrets and variables → Actions에 등록.
EC2가 프로젝트별로 분리되어 있으므로 서버별 Secret을 등록한다.

| Secret | 설명 | 예시 |
|--------|------|------|
| `USER_API_SERVER_HOST` | user-api EC2 IP | `10.0.1.10` |
| `ADMIN_API_SERVER_HOST` | admin-api EC2 IP | `10.0.1.20` |
| `SERVER_USER` | SSH 접속 유저 (공통) | `ec2-user` |
| `SSH_PRIVATE_KEY` | EC2 키페어 .pem (공통) | `-----BEGIN OPENSSH...` |

> GHCR(GitHub Container Registry)은 `GITHUB_TOKEN`으로 push하고,
> 서버에서는 PAT(Personal Access Token)로 pull한다(3.2절 참조).

#### 다중 서버 배포 (같은 프로젝트를 EC2 2대 이상에 배포)

HOST Secret에 쉼표로 IP를 나열하면 된다. 워크플로우 코드 변경은 불필요.

```
USER_API_SERVER_HOST = 10.0.1.10,10.0.1.11
```

배포 흐름:

```
git push origin deploy/user-api
        │
        ▼
deploy-user-api.yml
  → SERVER_HOST에 "10.0.1.10,10.0.1.11" 전달
        │
        ▼
deploy-backend.yml
  → JAR 빌드 → Docker 이미지 → GHCR push (1회)
        │
        ▼
appleboy/ssh-action (host: "10.0.1.10,10.0.1.11")
        │
        ├── 1) ssh ec2-user@10.0.1.10 → deploy.sh 실행 → 완료
        │
        └── 2) ssh ec2-user@10.0.1.11 → deploy.sh 실행 → 완료
```

> 빌드는 1번, 배포는 서버 수만큼 순차 실행된다.
> 첫 번째 서버 배포가 완료된 후 두 번째 서버에 배포하므로, 한 대는 항상 정상 운영 중이다.
>
> **원리**: `appleboy/ssh-action`이 host에 쉼표가 있으면 내부적으로 파싱하여
> 순서대로 SSH 접속 → 스크립트 실행을 반복한다. 첫 번째 서버가 성공하면
> 다음 서버로 진행하고, 실패하면 전체 중단된다.
> 워크플로우나 deploy.sh를 수정할 필요 없이 이 라이브러리의 내장 기능이다.

### 2.3 서버 요구사항

- **인스턴스**: t3a.large (2 vCPU, 8GB RAM)
- **OS**: Amazon Linux 2023 (AL2023)
- Docker Engine 24+ (`dnf install docker`)
- Nginx (`dnf install nginx`)
- cronie (`dnf install cronie`) — 로그 백업 cron용
- curl (AL2023 기본 포함)

---

## 3. 서버 초기 세팅

새 서버를 세팅할 때 아래를 순서대로 실행한다. user-api 기준으로 설명한다.

### 3.1 Docker + Nginx 설치

```bash
# Docker
sudo dnf install -y docker
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
newgrp docker

# Nginx + cronie (crontab용)
sudo dnf install -y nginx cronie
sudo systemctl enable --now nginx crond
```

### 3.2 GHCR 인증 (이미지 pull 용)

GitHub에서 PAT를 발급한다: Settings → Developer settings → Personal access tokens → `read:packages` 스코프.

```bash
echo "<GHCR_PAT>" | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin
```

> Docker credential은 `~/.docker/config.json`에 저장되어 이후 pull 시 자동 인증된다.

### 3.3 디렉토리 구조 생성

```bash
sudo mkdir -p /opt/deploy/{projects,nginx/conf.d}
sudo chown -R $USER:$USER /opt/deploy

sudo mkdir -p /app/{logs,backup}
sudo chown -R $USER:$USER /app
```

결과 구조:

```
/opt/deploy/
├── deploy.sh                          # 범용 배포 스크립트
├── projects/
│   └── user-api.env                   # 배포 설정
└── nginx/
    └── conf.d/
        └── user-api-upstream.conf     # deploy.sh가 자동 관리

/app/
├── logs/                              # 앱 로그 (컨테이너에서 볼륨 마운트)
│   ├── app.log                        # 현재 로그
│   └── app-2026-03-01.0.log           # 일별 롤링 파일
└── backup/                            # 로그 백업 (cron이 관리)

/etc/nginx/conf.d/
└── user-api.conf                      # Nginx server 블록 (수동 1회 작성)
```

### 3.4 파일 배치

프로젝트 디렉토리의 파일들을 서버에 복사한다.

```bash
# 배포 스크립트 (공용)
cp deploy.sh /opt/deploy/deploy.sh
chmod +x /opt/deploy/deploy.sh

# 프로젝트 설정 (user-api/ 디렉토리에서)
cp user-api.env /opt/deploy/projects/user-api.env
cp user-api-upstream.conf /opt/deploy/nginx/conf.d/user-api-upstream.conf
sudo cp user-api.conf /etc/nginx/conf.d/user-api.conf
```

> 복사 후 `.env`의 `IMAGE`, `SPRING_PROFILES`와 Nginx conf의 `server_name`을 실제 값으로 변경한다.

### 3.5 Nginx upstream 디렉토리 연결

```bash
# /etc/nginx/nginx.conf의 http 블록 안에 추가 (최초 1회)
sudo bash -c 'grep -q "/opt/deploy/nginx" /etc/nginx/nginx.conf || \
  sed -i "/http {/a\\    include /opt/deploy/nginx/conf.d/*.conf;" /etc/nginx/nginx.conf'

sudo nginx -t && sudo nginx -s reload
```

---

## 4. 레포지토리 구성

### 공용 파일

| 파일 | 배치 위치 | 설명 |
|------|-----------|------|
| [`deploy.sh`](deploy.sh) | 서버 `/opt/deploy/deploy.sh` | 범용 배포 스크립트 |
| [`backend.Dockerfile`](backend.Dockerfile) | `infra/docker/backend.Dockerfile` | 공용 Dockerfile |
| [`deploy-backend.yml`](deploy-backend.yml) | `.github/workflows/deploy-backend.yml` | 재사용 워크플로우 |

### 프로젝트별 파일

| 파일 | 배치 위치 |
|------|-----------|
| [`user-api/deploy-user-api.yml`](user-api/deploy-user-api.yml) | `.github/workflows/deploy-user-api.yml` |
| [`user-api/user-api.env`](user-api/user-api.env) | 서버 `/opt/deploy/projects/user-api.env` |
| [`user-api/user-api-upstream.conf`](user-api/user-api-upstream.conf) | 서버 `/opt/deploy/nginx/conf.d/user-api-upstream.conf` |
| [`user-api/user-api.conf`](user-api/user-api.conf) | 서버 `/etc/nginx/conf.d/user-api.conf` |

> admin-api도 동일한 구조. [`admin-api/`](admin-api/) 디렉토리 참조.

### 수정 규칙

`docs/backend/deployment/`가 원본이다. 배포 설정을 변경할 때는 반드시 아래 순서를 따른다.

1. **`docs/backend/deployment/`의 원본 파일을 먼저 수정**한다
2. 수정한 내용을 실제 배치 위치에 동일하게 반영한다

| 원본 (docs) | 배치 위치 |
|-------------|-----------|
| `deploy-backend.yml` | `.github/workflows/deploy-backend.yml` |
| `user-api/deploy-user-api.yml` | `.github/workflows/deploy-user-api.yml` |
| `admin-api/deploy-admin-api.yml` | `.github/workflows/deploy-admin-api.yml` |
| `backend.Dockerfile` | `infra/docker/backend.Dockerfile` |

> 원본과 배치 파일은 항상 동일한 내용을 유지해야 한다. 배치 위치만 직접 수정하면 원본과 불일치가 발생한다.

---

## 5. 배포 & 롤백

### 5.1 자동 배포

프로젝트 전용 브랜치에 push하면 해당 프로젝트만 배포된다. 변경된 파일과 무관하게 항상 동작한다.

| 브랜치 | 배포 대상 |
|--------|----------|
| `deploy/user-api` | user-api |
| `deploy/admin-api` | admin-api |

```
git push origin deploy/user-api
  → deploy-user-api.yml 트리거
    → build JAR → Docker image → GHCR push → SSH → deploy.sh → 완료
```

> A 프로젝트 코드도 함께 바뀌었더라도 B 브랜치에만 push하면 B만 배포된다.

### 5.2 수동 배포

**(1) GitHub UI**: Actions 탭 → 워크플로우 선택 → "Run workflow" 클릭

**(2) 서버에서 직접**:

```bash
# 최신 이미지
/opt/deploy/deploy.sh user-api latest

# 특정 커밋 이미지
/opt/deploy/deploy.sh user-api abc1234
```

### 5.3 롤백

이전 커밋의 이미지 태그로 배포하면 즉시 롤백된다. 재빌드 불필요.

```bash
# 이전 이미지 태그 확인
docker images ghcr.io/namgyunkim/mono-repo/user-api --format "{{.Tag}}\t{{.CreatedAt}}"

# 롤백 실행
/opt/deploy/deploy.sh user-api <이전-태그>
```

---

## 6. 새 프로젝트 추가 체크리스트

새로운 Spring Boot 앱(예: `payment-api`)을 배포 대상에 추가할 때:

### 레포지토리

- [ ] `apps/payment-api/`에 `/api/health` 엔드포인트 존재 확인
- [ ] `docs/backend/deployment/payment-api/` 디렉토리 생성
- [ ] 기존 프로젝트 디렉토리(예: `user-api/`)를 복사하고 프로젝트명 변경
  - `deploy-payment-api.yml` (브랜치: `deploy/payment-api`)
  - `payment-api.env`
  - `payment-api-upstream.conf`
  - `payment-api.conf`

### 서버

- [ ] [`deploy.sh`](deploy.sh)를 `/opt/deploy/deploy.sh`에 복사 (이미 있으면 생략)
- [ ] `payment-api/` 디렉토리의 파일들을 서버에 배치 (3.4절 참조)
- [ ] `.env`와 Nginx conf의 서버별 값 수정
- [ ] `sudo nginx -t && sudo nginx -s reload`

> `deploy.sh`와 서버 디렉토리 구조는 모든 서버에서 동일하다. 프로젝트명만 다르다.

---

## 7. 운영

### 로그 확인

```bash
# 파일 로그 실시간 확인
tail -f /app/logs/app.log

# Docker 컨테이너 로그 (stdout)
docker logs -f user-api-blue

# 최근 100줄
docker logs user-api-green --tail 100

# 일별 로그 파일 목록
ls -lt /app/logs/
```

### 로그 백업 (cron)

일별 롤링 파일을 압축하여 `/app/backup`에 보관한다. 서버에서 최초 1회 설정.

```bash
# crontab 등록
crontab -e

# 매일 새벽 2시: 2일 이상 된 롤링 파일을 압축 후 /app/backup으로 이동
0 2 * * * find /app/logs -name "app-*.log" -mtime +1 -exec gzip {} \; -exec mv {}.gz /app/backup/ \;

# (선택) 90일 이상 된 백업 파일 자동 삭제
0 3 * * * find /app/backup -name "*.gz" -mtime +90 -delete
```

> logback이 `/app/logs`에 최근 30일 롤링 파일을 유지하고,
> cron이 오래된 파일을 압축하여 `/app/backup`에 장기 보관한다.

### 현재 상태 확인

```bash
# 어떤 컬러가 활성인지
cat /opt/deploy/projects/user-api.active

# 실행 중인 컨테이너
docker ps --filter "name=user-api"

# 각 포트 health check
curl -s http://localhost:8081/api/health
curl -s http://localhost:8082/api/health
```

### 오래된 이미지 정리

```bash
# 사용하지 않는 이미지 일괄 제거
docker image prune -a --filter "until=168h" -f
```

---

## 8. 트러블슈팅

| 증상 | 원인 | 해결 |
|------|------|------|
| Health check 실패 | 앱 기동 느림 | `.env`의 `HEALTH_TIMEOUT` 증가 |
| Health check 실패 | DB 연결 불가 | `application-prod.yml`의 DB 접속 정보 확인, DB 서버 방화벽/보안그룹 점검 |
| Nginx reload 실패 | 설정 문법 오류 | `sudo nginx -t`로 상세 에러 확인 |
| 이미지 pull 실패 | GHCR 인증 만료 | `docker login ghcr.io` 재실행 |
| 포트 충돌 | 이전 컨테이너 미종료 | `docker rm -f <컨테이너명>` 후 재배포 |
| Container exited | 앱 크래시 | `docker logs <컨테이너명>` 확인 |

---

## 9. 전체 흐름 요약

```
[개발자]
    │
    ├── git push origin deploy/user-api
    │
[GitHub Actions — deploy-user-api.yml]
    ├── checkout
    ├── ./gradlew :apps:user-api:bootJar
    ├── docker build → ghcr.io/.../user-api:abc1234
    ├── docker push
    └── ssh ec2-user@EC2-1 "/opt/deploy/deploy.sh user-api abc1234"
            │
[EC2-1 deploy.sh]
            ├── 현재 활성: blue(8081)
            ├── docker pull ...user-api:abc1234
            ├── docker run → user-api-green (8082)
            ├── health check → OK
            ├── nginx upstream → 127.0.0.1:8082
            ├── nginx reload
            ├── docker stop user-api-blue
            └── echo "green" > user-api.active
            │
[ALB]       └── 클라이언트 → ALB(:443) → EC2-1 Nginx(:80) → Green(8082)
```

---

## TODO

- [x] `/app/logs`에 프로그램 실행 시 `app.log`로 로그 남기기 및 매일 백업 전략 수립
- [ ] 배포 실패 시 알림/대응 전략 수립 (Slack 알림, 자동 롤백 등)
- [ ] 배포 성공 시 후속 전략 수립 (배포 이력 기록, 알림 등)
