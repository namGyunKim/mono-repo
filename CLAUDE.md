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

# MCP 서버 (Serena, Context7)

이 워크스페이스에는 두 개의 MCP 서버가 연결되어 있다.

## Serena — 시맨틱 코드 분석/편집

- 심볼(클래스, 메서드, 필드) 단위로 코드를 탐색·편집한다
- 파일 전체를 읽기보다 `get_symbols_overview` → `find_symbol(include_body=True)` 순서로 필요한 부분만 읽는다
- 심볼 본문 교체(`replace_symbol_body`), 삽입(`insert_before/after_symbol`), 리네임(`rename_symbol`) 을 활용한다
- 참조 추적이 필요하면 `find_referencing_symbols`를 사용한다
- 프로젝트 활성화: 대화 시작 시 `activate_project`로 이 워크스페이스를 활성화한다

## Context7 — 최신 라이브러리 문서 조회

- `resolve-library-id`로 라이브러리 ID를 먼저 확인한 뒤 `query-docs`로 문서를 조회한다
- 학습 데이터 컷오프 이후 변경된 API나 최신 버전 문서 확인 시 사용한다
