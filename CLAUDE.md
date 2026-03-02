<!-- nx configuration start-->
<!-- Leave the start & end comments to automatically receive updates. -->

# General Guidelines for working with Nx

- For navigating/exploring the workspace, invoke the `nx-workspace` skill first - it has patterns for querying projects, targets, and dependencies
- When running tasks (for example build, lint, test, e2e, etc.), always prefer running the task through `nx` (i.e. `nx run`, `nx run-many`, `nx affected`) instead of using the underlying tooling directly
- Prefix nx commands with the workspace's package manager (e.g., `pnpm nx build`, `npm exec nx test`) - avoids using globally installed CLI
- You have access to the Nx MCP server and its tools, use them to help the user
- For Nx plugin best practices, check `node_modules/@nx/<plugin>/PLUGIN.md`. Not all plugins have this file - proceed without it if unavailable.
- NEVER guess CLI flags - always check nx_docs or `--help` first when unsure

## Scaffolding & Generators

- For scaffolding tasks (creating apps, libs, project structure, setup), ALWAYS invoke the `nx-generate` skill FIRST before exploring or calling MCP tools

## When to use nx_docs

- USE for: advanced config options, unfamiliar flags, migration guides, plugin configuration, edge cases
- DON'T USE for: basic generator syntax (`nx g @nx/react:app`), standard commands, things you already know
- The `nx-generate` skill handles generator discovery internally - don't call nx_docs just to look up generator syntax

<!-- nx configuration end-->

---

# NX 워크스페이스 사용 가이드 (한국어)

- 워크스페이스 탐색 시 `nx-workspace` 스킬을 먼저 사용한다
- 빌드/린트/테스트 등 태스크는 반드시 `nx`를 경유해서 실행한다 (`nx run`, `nx run-many`, `nx affected`)
- nx 명령에는 패키지 매니저 접두사를 붙인다 (예: `pnpm nx build`) — 전역 CLI 사용 금지
- NX 플러그인 모범 사례는 `node_modules/@nx/<plugin>/PLUGIN.md` 확인 (없으면 무시)
- CLI 플래그를 추측하지 않는다 — 확실하지 않으면 `--help` 먼저 확인
- 스캐폴딩(앱/라이브러리 생성) 작업 시 `nx-generate` 스킬을 먼저 호출한다

---

# 영역별 개발 지침 (CRITICAL)

각 영역의 코드를 생성/수정할 때는 해당 영역의 지침 문서를 **반드시 읽고 준수**한다.

| 영역 | 지침 문서 |
|------|-----------|
| 백엔드 (Spring Boot) | `docs/backend/README.md`, `docs/backend/RULES.md` |

> **백엔드 코드를 생성/수정할 때는 반드시 `docs/backend/RULES.md`를 먼저 읽고 모든 규칙을 준수한다.**

---

# 첨부 이미지 확인

사용자가 "첨부한 이미지 확인해줘" 등 이미지 참조를 요청하면, 프로젝트 루트 경로의 `*.png` 파일을 확인한다.

---

# AI 자율 실행 규칙 (CRITICAL)

- 코드 생성/수정/리팩토링/삭제는 **권한 확인 없이 자율 진행**한다
- 빌드(`pnpm nx build`, `./gradlew build`), 테스트(`pnpm nx test`), 린트 실행도 자율 진행
- 리팩토링 중 발견된 위반 사항은 즉시 수정한다
- 파일 읽기/탐색/검색은 자율 진행
- **GitHub 설정 변경**(Branch Protection, Secrets, Actions 등) 시 관련 문서(`docs/CI_STRATEGY.md` 등)를 즉시 최신화한다

---

# MCP 서버 (Serena -> mcp__serena__*, Context7 -> mcp__context7__*, Postgres -> mcp__postgres__*, Playwright -> mcp__playwright__*)

이 워크스페이스에는 네 개의 MCP 서버가 연결되어 있다.

> **대화 시작 시 MCP 활성화 확인 (CRITICAL)**
>
> 매 대화(세션) 시작 시 아래 순서를 **반드시** 실행한다:
> 1. `mcp__serena__activate_project("mono-repo")` — Serena 프로젝트 활성화
> 2. `mcp__serena__check_onboarding_performed()` — 온보딩 상태 확인 (미수행 시 `onboarding` 실행)
>
> 이 단계를 건너뛰면 시맨틱 코드 도구를 사용할 수 없다.

## Serena — 시맨틱 코드 분석/편집

- 심볼(클래스, 메서드, 필드) 단위로 코드를 탐색·편집한다
- 파일 전체를 읽기보다 `get_symbols_overview` → `find_symbol(include_body=True)` 순서로 필요한 부분만 읽는다
- 프로젝트 활성화: 대화 시작 시 `activate_project`로 이 워크스페이스를 활성화한다
- 온보딩: `check_onboarding_performed`로 확인하여 **미수행 상태일 때만 최초 1회** `onboarding`을 실행한다 (이후 대화에서는 불필요)

### MCP 도구 목록

| 도구명 | 설명 |
|--------|------|
| `mcp__serena__get_symbols_overview` | 파일의 심볼(클래스, 메서드, 필드) 개요 조회 |
| `mcp__serena__find_symbol` | 이름 패턴으로 심볼 검색 (`include_body=True`로 본문 포함) |
| `mcp__serena__find_referencing_symbols` | 특정 심볼을 참조하는 코드 위치 추적 |
| `mcp__serena__replace_symbol_body` | 심볼 본문(메서드, 클래스 등)을 교체 |
| `mcp__serena__insert_before_symbol` | 심볼 정의 앞에 코드 삽입 |
| `mcp__serena__insert_after_symbol` | 심볼 정의 뒤에 코드 삽입 |
| `mcp__serena__rename_symbol` | 심볼 이름을 코드베이스 전체에서 변경 |
| `mcp__serena__search_for_pattern` | 정규식 기반 코드 패턴 검색 |
| `mcp__serena__list_dir` | 디렉토리 내용 조회 |
| `mcp__serena__find_file` | 파일명/파일 마스크로 파일 검색 |
| `mcp__serena__write_memory` / `read_memory` | 프로젝트 메모리 저장/읽기 |

## Context7 — 최신 라이브러리 문서 조회

- 학습 데이터 컷오프 이후 변경된 API나 최신 버전 문서 확인 시 사용한다
- 반드시 `resolve-library-id` → `query-docs` 순서로 호출한다

### MCP 도구 목록

| 도구명 | 설명 |
|--------|------|
| `mcp__context7__resolve-library-id` | 라이브러리 이름으로 Context7 ID를 검색 (항상 먼저 호출) |
| `mcp__context7__query-docs` | 검색된 ID로 최신 문서/코드 예제 조회 |

## Postgres — 데이터베이스 직접 조회

### 왜 사용하는가

- **엔티티 ↔ 스키마 정합성 검증**: JPA 엔티티 수정 시 실제 DB 테이블과 비교하여 불일치를 사전에 발견한다
- **쿼리 성능 분석**: `EXPLAIN ANALYZE`를 에이전트 내에서 바로 실행하여 느린 쿼리 원인을 분석한다
- **마이그레이션 검증**: Flyway/Liquibase 적용 후 결과를 즉시 확인한다
- **디버깅**: 버그 재현 시 실제 데이터를 조회하며 원인을 추적한다
- DB 구조를 이해한 상태에서 코드를 생성/수정하므로 **정확도가 높아진다**

### 사용 규칙

- **읽기 전용 계정(`claude_readonly`)으로만 접속**한다 — `INSERT`, `UPDATE`, `DELETE`, `DROP` 등 쓰기 작업은 불가
- 테이블 구조 확인, `SELECT` 쿼리 실행, 관계(FK/인덱스) 파악 용도로만 사용한다
- 설정 파일: 프로젝트 루트 `.mcp.json` (git에 포함, 읽기 전용 계정이므로 안전)

### 최초 설정 방법 (새 팀원용)

PostgreSQL(14 이상)에서 아래 SQL을 실행하여 읽기 전용 계정을 생성한다:

```sql
CREATE USER claude_readonly WITH PASSWORD 'readonly_pass';
GRANT CONNECT ON DATABASE base_project TO claude_readonly;
GRANT pg_read_all_data TO claude_readonly;
```

> `pg_read_all_data`는 PostgreSQL 14+에서 제공하는 내장 롤로, 모든 스키마·테이블에 대해 `SELECT` 권한을 자동 부여한다.
> 테이블 생성 시점이나 소유자와 무관하게 항상 적용된다.

이후 Claude Code를 재시작하면 `.mcp.json`이 자동으로 로드되어 DB MCP가 활성화된다.

### 동작 방식

1. Claude Code가 시작되면 프로젝트 루트의 `.mcp.json`을 읽는다
2. `@modelcontextprotocol/server-postgres` 서버가 `npx`로 자동 실행된다
3. 서버는 `.mcp.json`에 명시된 접속 문자열(`postgresql://claude_readonly:readonly_pass@localhost:5432/base_project`)로 PostgreSQL에 연결한다
4. `claude_readonly` 계정은 `SELECT` 권한만 가지므로, 모든 조회는 읽기 전용으로 수행된다
5. 연결된 DB의 **스키마 정보(테이블, 컬럼, 타입, FK, 인덱스)가 리소스로 자동 노출**되어 에이전트가 참조할 수 있다
6. 에이전트는 `query` 도구를 통해 `SELECT` 쿼리를 직접 실행하고 결과를 받을 수 있다

### MCP 도구 목록

| 도구명 | 설명 |
|--------|------|
| `mcp__postgres__query` | 읽기 전용 SQL 쿼리 실행 (`sql` 파라미터에 SELECT 문 전달) |
| `ListMcpResourcesTool(server="postgres")` | DB 스키마 리소스 목록 조회 (SQL 없이 테이블/컬럼 정보) |
| `ReadMcpResourceTool(server="postgres", uri=...)` | 특정 테이블의 스키마 리소스 상세 읽기 |

### 자주 쓰는 쿼리

| 목적 | SQL |
|------|-----|
| 전체 테이블 목록 | `SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename` |
| 테이블 컬럼 구조 | `SELECT column_name, data_type, is_nullable, column_default FROM information_schema.columns WHERE table_name = '테이블명' ORDER BY ordinal_position` |
| FK 관계 확인 | `SELECT tc.constraint_name, kcu.column_name, ccu.table_name AS foreign_table, ccu.column_name AS foreign_column FROM information_schema.table_constraints tc JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = '테이블명'` |
| 인덱스 목록 | `SELECT indexname, indexdef FROM pg_indexes WHERE tablename = '테이블명'` |
| 쿼리 성능 분석 | `EXPLAIN ANALYZE SELECT ...` |
| 데이터 샘플 조회 | `SELECT * FROM 테이블명 ORDER BY id DESC LIMIT 10` |

### 사용 예시

| 요청 | 에이전트 동작 |
|------|--------------|
| `"테이블 목록 보여줘"` | DB 리소스에서 전체 테이블/컬럼/타입 정보를 조회하여 반환 |
| `"users 테이블 구조 확인해줘"` | 해당 테이블의 컬럼명, 데이터 타입, PK, FK, 인덱스 정보를 조회 |
| `"최근 가입한 유저 10명 조회해줘"` | `SELECT * FROM users ORDER BY created_at DESC LIMIT 10` 실행 후 결과 반환 |
| `"이 JPA 엔티티가 DB 스키마와 맞는지 검증해줘"` | 엔티티 클래스의 필드와 실제 테이블 컬럼을 비교하여 불일치 항목 보고 |
| `"이 쿼리 성능 분석해줘"` | `EXPLAIN ANALYZE`로 실행 계획을 조회하여 병목 지점 분석 |
| `"Member 엔티티에 새 필드 추가하려는데 현재 테이블 구조 먼저 확인"` | 테이블 스키마를 조회한 뒤, 기존 구조에 맞게 엔티티 코드 수정 |

## Playwright — 브라우저 자동화 / E2E 검증

- 실제 브라우저를 제어하여 **페이지 탐색, 클릭, 폼 입력, 스크린샷** 등을 수행한다
- 프론트엔드 개발 중 **UI 동작을 대화 내에서 직접 검증**할 수 있다
- E2E 테스트 시나리오를 실행하거나, 디버깅 시 화면 상태를 확인하는 용도로 사용한다

### 사용 규칙

- dev 서버가 실행 중이어야 브라우저로 접근할 수 있다 (예: `pnpm nx dev @mono-repo/web`)
- 스냅샷(`browser_snapshot`)은 접근성 트리 기반으로 요소를 파악하며, 인터랙션 시 `ref` 값을 사용한다
- 스크린샷(`browser_take_screenshot`)은 시각적 확인용이며, 요소 조작에는 스냅샷을 사용한다
- 작업 완료 후 `browser_close`로 브라우저를 닫는다

### MCP 도구 목록

| 도구명 | 설명 |
|--------|------|
| `mcp__playwright__browser_navigate` | URL로 페이지 이동 |
| `mcp__playwright__browser_snapshot` | 현재 페이지의 접근성 스냅샷 조회 (요소 ref 포함) |
| `mcp__playwright__browser_click` | 요소 클릭 (`ref` 지정) |
| `mcp__playwright__browser_type` | 요소에 텍스트 입력 |
| `mcp__playwright__browser_fill_form` | 여러 폼 필드를 한번에 입력 |
| `mcp__playwright__browser_select_option` | 드롭다운 옵션 선택 |
| `mcp__playwright__browser_hover` | 요소에 마우스 호버 |
| `mcp__playwright__browser_drag` | 드래그 앤 드롭 |
| `mcp__playwright__browser_press_key` | 키보드 키 입력 |
| `mcp__playwright__browser_take_screenshot` | 페이지/요소 스크린샷 캡처 |
| `mcp__playwright__browser_evaluate` | 페이지에서 JavaScript 실행 |
| `mcp__playwright__browser_console_messages` | 브라우저 콘솔 메시지 조회 |
| `mcp__playwright__browser_network_requests` | 네트워크 요청 목록 조회 |
| `mcp__playwright__browser_wait_for` | 텍스트 출현/소멸 또는 시간 대기 |
| `mcp__playwright__browser_handle_dialog` | alert/confirm/prompt 다이얼로그 처리 |
| `mcp__playwright__browser_file_upload` | 파일 업로드 |
| `mcp__playwright__browser_tabs` | 탭 목록 조회/생성/닫기/선택 |
| `mcp__playwright__browser_resize` | 브라우저 창 크기 조절 |
| `mcp__playwright__browser_close` | 브라우저 닫기 |
| `mcp__playwright__browser_run_code` | Playwright 코드 스니펫 직접 실행 |

### 사용 예시

| 요청 | 에이전트 동작 |
|------|--------------|
| `"localhost:3000 열어봐"` | dev 서버 실행 확인 후 `browser_navigate`로 접속 |
| `"로그인 폼 테스트해줘"` | 스냅샷으로 폼 요소 파악 → `browser_fill_form`으로 입력 → 제출 후 결과 확인 |
| `"현재 화면 스크린샷 찍어줘"` | `browser_take_screenshot`으로 캡처 |
| `"콘솔 에러 있는지 확인해줘"` | `browser_console_messages(level="error")`로 에러 로그 조회 |
| `"버튼 클릭하면 API 호출되는지 확인"` | 버튼 클릭 후 `browser_network_requests`로 요청 확인 |
