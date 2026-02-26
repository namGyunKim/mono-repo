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
