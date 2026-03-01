# 배포 전략 요약

> 상세 가이드: [docs/backend/deployment/BLUE_GREEN.md](../backend/deployment/BLUE_GREEN.md)

---

## 한줄 요약

**AWS 올인원** — ALB + EC2 2대 Blue/Green 무중단 배포, ACM SSL, WAF 보안, S3 + CloudFront 이미지 서빙.

---

## 전체 아키텍처

```
                    [클라이언트]
                        │
              ┌─────────▼──────────┐
              │     Route 53       │
              │   (DNS 라우팅)      │
              └────┬──────────┬────┘
                   │          │
         ┌─────────▼───┐  ┌──▼──────────────┐
         │     ALB     │  │  CloudFront      │
         │  + ACM SSL  │  │  (이미지 CDN)     │
         │  + WAF      │  │                  │
         └──┬───────┬──┘  └───────┬──────────┘
            │       │             │
     ┌──────▼──┐ ┌──▼──────┐  ┌──▼──────────┐
     │  EC2-1  │ │  EC2-2  │  │  S3 (비공개) │
     │ user-api│ │admin-api│  │  이미지 저장  │
     │ B/G     │ │ B/G     │  └─────────────┘
     └────┬────┘ └────┬────┘
          │           │
       ┌──▼───────────▼──┐
       │   RDS / EC2 DB   │
       │   PostgreSQL     │
       └─────────────────┘
```

---

## AWS 서비스 역할

| 서비스            | 역할                       | 비고                              |
|----------------|--------------------------|---------------------------------|
| **ALB**        | 로드밸런서, HTTPS 종료, EC2 라우팅 | 리스너: 443(HTTPS) → 타겟그룹(80)      |
| **ACM**        | SSL/TLS 인증서              | ALB에 연결, 자동 갱신, 무료              |
| **WAF**        | 웹 방화벽                    | ALB에 연결, SQL Injection/XSS 등 차단 |
| **EC2**        | 앱 서버 (t3a.large x 2)     | 1서버 1프로젝트, 각각 Blue/Green 배포     |
| **S3**         | 이미지 저장소                  | 비공개 버킷, 직접 접근 차단                |
| **CloudFront** | 이미지 CDN                  | OAC로 S3 비공개 접근, 캐싱 + 글로벌 배포     |
| **Route 53**   | DNS                      | 도메인 → ALB, 이미지 도메인 → CloudFront |

---

## 네트워크 흐름

### API 요청

```
클라이언트 → Route 53 → ALB (WAF 검사 → ACM SSL 종료) → EC2 Nginx(:80) → Docker 컨테이너
```

- ALB가 HTTPS를 처리하므로 EC2의 Nginx는 **HTTP(:80)만** 수신
- WAF가 ALB 앞단에서 악성 요청을 필터링

### 이미지 요청

```
클라이언트 → Route 53 → CloudFront (캐시) → S3 (비공개, OAC 인증)
```

- S3 버킷은 퍼블릭 접근 차단, CloudFront OAC(Origin Access Control)를 통해서만 접근
- CloudFront가 엣지 캐싱으로 S3 직접 요청을 최소화

---

## 배포 방식

### 자동 배포

배포 전용 브랜치에 push하면 해당 프로젝트만 자동 배포된다.

**현재 (테스트 서버만):**

| 브랜치                | 배포 대상     | 환경     |
|--------------------|-----------|--------|
| `deploy/user-api`  | user-api  | 테스트 서버 |
| `deploy/admin-api` | admin-api | 테스트 서버 |

**main 도입 후 (테스트 + 실서버):**

| 브랜치                | 배포 대상     | 환경     |
|--------------------|-----------|--------|
| `stage/user-api`   | user-api  | 테스트 서버 |
| `stage/admin-api`  | admin-api | 테스트 서버 |
| `deploy/user-api`  | user-api  | 실서버    |
| `deploy/admin-api` | admin-api | 실서버    |

```
git push origin stage/user-api    → 테스트 서버 배포
git push origin deploy/user-api   → 실서버 배포
  → GitHub Actions 자동 실행
    → JAR 빌드 → Docker 이미지 → GHCR push → SSH 배포
```

> 두 워크플로우 모두 같은 `backend-cd.yml`을 호출한다.
> GitHub Secret에 등록된 서버 IP만 다르게 지정하여 대상 서버를 결정한다.

### 수동 배포 / 롤백

```bash
# 서버에서 직접 배포
/opt/deploy/deploy.sh user-api latest

# 롤백 (이전 이미지 태그로 재배포, 재빌드 불필요)
/opt/deploy/deploy.sh user-api <이전-태그>
```

---

## Blue/Green 전환 흐름

각 EC2 내부에서 Docker 컨테이너 단위로 Blue/Green 전환한다.

```
 현재 상태                    배포 중                        완료
 ─────────                  ──────                       ────
 Nginx → Blue(8081) ✅       Nginx → Blue(8081) ✅         Nginx → Green(8082) ✅
         Green ❌                    Green(8082) 기동 중           Blue 종료
```

| 단계  | 동작                         | 실패 시         |
|-----|----------------------------|--------------|
| 1   | 새 이미지 pull                 | 중단, 기존 유지    |
| 2   | 그린(8082) 컨테이너 기동           | 중단, 기존 유지    |
| 3   | 그린 health check            | 그린 제거, 블루 유지 |
| 4   | Nginx upstream 전환 + reload | 그린 제거, 블루 유지 |
| 5   | 블루 graceful shutdown (30초) | —            |
| 6   | 활성 컬러 기록                   | —            |

> 어느 단계에서 실패해도 기존 컨테이너가 트래픽을 처리하므로 **서비스 중단 없음**.

---

## 보안 구성

| 계층      | 방어        | 설명                                             |
|---------|-----------|------------------------------------------------|
| DNS     | Route 53  | AWS 관리형 DNS, DDoS 기본 방어                        |
| L7 방화벽  | WAF → ALB | SQL Injection, XSS, Bot 차단 (AWS Managed Rules) |
| SSL/TLS | ACM → ALB | HTTPS 종료, 자동 갱신, EC2에는 HTTP만 전달                |
| 네트워크    | 보안그룹      | EC2: ALB에서만 80 허용, S3: CloudFront OAC만 허용      |
| 이미지 저장  | S3 비공개    | 퍼블릭 접근 차단, CloudFront OAC 경유만 허용               |

---

## 이미지 서빙 (S3 + CloudFront)

| 항목                | 설정                                                 |
|-------------------|----------------------------------------------------|
| S3 버킷             | 비공개 (Block Public Access 전체 활성화)                   |
| CloudFront Origin | S3, OAC(Origin Access Control) 인증                  |
| 캐시 정책             | 이미지: 장기 캐시 (Cache-Control: max-age)                |
| 도메인               | `images.example.com` → CloudFront (Route 53 CNAME) |

```
앱 서버 (업로드)                     클라이언트 (조회)
     │                                    │
     ▼                                    ▼
  S3 (비공개)  ←── OAC 인증 ──  CloudFront (캐시)
```

> 앱에서 S3에 직접 업로드, 클라이언트는 CloudFront URL로 조회.
> S3 직접 URL은 접근 불가.

---

## 핵심 원칙

| 원칙        | 설명                                    |
|-----------|---------------------------------------|
| AWS 올인원   | SSL, WAF, CDN, 스토리지 모두 AWS 서비스로 통일    |
| 빌드/배포 분리  | CI에서 빌드, 서버에서는 pull + 기동만             |
| 불변 아티팩트   | Docker 이미지 = 배포 단위, 서버에 소스코드 없음       |
| 1서버 1프로젝트 | EC2마다 하나의 프로젝트만 운영                    |
| 비공개 원칙    | S3 비공개 + CloudFront OAC, EC2 직접 접근 차단 |

---

## 로깅 전략

| 항목    | 내용                                       |
|-------|------------------------------------------|
| 로그 위치 | `/app/logs/app.log` (호스트에 볼륨 마운트)        |
| 롤링    | 일별 자동 롤링, 최근 30일 보관, 파일당 100MB           |
| 백업    | cron이 매일 새벽 2시에 압축 → `/app/backup/`으로 이동 |
| 백업 보관 | 90일 초과 시 자동 삭제                           |

---

## 새 프로젝트 추가 시

1. `deploy-<프로젝트명>.yml` + `stage-<프로젝트명>.yml` 워크플로우 파일 생성 (기존 파일 복사, 프로젝트명만 변경)
2. EC2 신규 생성 + 서버 세팅 9단계 실행
3. ALB 타겟그룹에 새 EC2 등록
4. 배포 브랜치 생성: `git push origin develop:deploy/<프로젝트명>`
5. GitHub Secrets에 서버 IP 등록 (`<PROJECT>_SERVER_HOST`, `STAGE_<PROJECT>_SERVER_HOST`)
6. 공통 워크플로우(`backend-cd.yml`)와 배포 스크립트(`deploy.sh`)는 수정 불필요
