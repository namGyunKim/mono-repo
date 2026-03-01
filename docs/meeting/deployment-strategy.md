# 배포 전략 요약

> 상세 가이드: [docs/backend/deployment/BLUE_GREEN.md](../backend/deployment/BLUE_GREEN.md)

---

## 한줄 요약

서버 1대에서 **Docker + Nginx + GitHub Actions**를 이용한 **Blue/Green 무중단 배포**.

---

## 인프라 구성

| 항목 | 사양 |
|------|------|
| 앱 서버 | EC2 t3a.large (2 vCPU, 8GB RAM) |
| OS | Amazon Linux 2023 |
| DB 서버 | 별도 서버, PostgreSQL |
| 컨테이너 레지스트리 | GitHub Container Registry (GHCR) |
| CI/CD | GitHub Actions |

```
  GitHub Actions                          앱 서버 (EC2)
 ┌────────────────┐        SSH       ┌──────────────────────┐
 │ JAR 빌드       │ ──────────────→ │ Nginx (:80/:443)     │
 │ Docker 이미지   │                  │  └→ Blue(8081)       │
 │ GHCR push      │                  │  or Green(8082)      │
 └────────────────┘                  └──────────┬───────────┘
                                                │
                                     ┌──────────▼───────────┐
                                     │ DB 서버 (PostgreSQL)  │
                                     └──────────────────────┘
```

---

## 배포 방식

### 자동 배포

배포 전용 브랜치에 push하면 해당 프로젝트만 자동 배포된다.

| 브랜치 | 배포 대상 |
|--------|----------|
| `deploy/user-api` | user-api |
| `deploy/admin-api` | admin-api |

```
git push origin deploy/user-api
  → GitHub Actions 자동 실행
    → JAR 빌드 → Docker 이미지 → GHCR push → SSH로 서버 배포
```

### 수동 배포 / 롤백

```bash
# 서버에서 직접 배포
/opt/deploy/deploy.sh user-api latest

# 롤백 (이전 이미지 태그로 재배포, 재빌드 불필요)
/opt/deploy/deploy.sh user-api <이전-태그>
```

---

## Blue/Green 전환 흐름

블루가 운영 중일 때 새 배포가 시작되면:

```
 현재 상태                    배포 중                        완료
 ─────────                  ──────                       ────
 Nginx → Blue(8081) ✅       Nginx → Blue(8081) ✅         Nginx → Green(8082) ✅
         Green ❌                    Green(8082) 기동 중           Blue 종료
```

| 단계 | 동작 | 실패 시 |
|------|------|---------|
| 1 | 새 이미지 pull | 중단, 기존 유지 |
| 2 | 그린(8082) 컨테이너 기동 | 중단, 기존 유지 |
| 3 | 그린 health check | 그린 제거, 블루 유지 |
| 4 | Nginx upstream 전환 + reload | 그린 제거, 블루 유지 |
| 5 | 블루 graceful shutdown (30초) | — |
| 6 | 활성 컬러 기록 | — |

> 어느 단계에서 실패해도 기존 컨테이너가 트래픽을 처리하므로 **서비스 중단 없음**.

---

## 핵심 원칙

| 원칙 | 설명 |
|------|------|
| 빌드/배포 분리 | CI에서 빌드, 서버에서는 pull + 기동만 |
| 불변 아티팩트 | Docker 이미지 = 배포 단위, 서버에 소스코드 없음 |
| 포트 컨벤션 | 모든 서버 동일: Blue=8081, Green=8082 |
| 1서버 1프로젝트 | 서버마다 하나의 프로젝트만 운영 |
| 재사용 | 프로젝트명만 바꾸면 동일 구조 적용 |

---

## 로깅 전략

| 항목 | 내용 |
|------|------|
| 로그 위치 | `/app/logs/app.log` (호스트에 볼륨 마운트) |
| 롤링 | 일별 자동 롤링, 최근 30일 보관, 파일당 100MB |
| 백업 | cron이 매일 새벽 2시에 압축 → `/app/backup/`으로 이동 |
| 백업 보관 | 90일 초과 시 자동 삭제 |

---

## 새 프로젝트 추가 시

1. `deploy-<프로젝트명>.yml` 워크플로우 파일 생성 (기존 파일 복사, 프로젝트명만 변경)
2. 서버에 `.env` + Nginx conf 배치
3. 배포 브랜치 생성: `git push origin main:deploy/<프로젝트명>`
4. 공통 워크플로우(`deploy-backend.yml`)와 배포 스크립트(`deploy.sh`)는 수정 불필요
