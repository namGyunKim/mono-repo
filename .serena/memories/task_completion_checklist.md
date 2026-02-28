# Task Completion Checklist

코드 작성/수정 완료 후 반드시 확인할 항목:

## 1. RULES.md 최종 점검
- `docs/backend/RULES.md`를 다시 확인하여 규칙 누락 없는지 최종 점검

## 2. 코드 품질
- [ ] DTO는 record + from/of 정적 팩토리
- [ ] 외부 `new DTO(...)` 없는지 확인
- [ ] CQRS 분리 준수 (Command/Query 상호 호출 금지)
- [ ] DDD Bounded Context 경계 준수 (Port 경유)
- [ ] Hexagonal Architecture 의존 방향 확인
- [ ] `final` 기본값 적용 (파라미터, 지역변수, 필드 모두)
- [ ] Text Block 사용 (`"\n"` 금지)
- [ ] Jackson import: `tools.jackson.*`

## 3. API 규칙
- [ ] `RestApiController`로 응답
- [ ] `version = ApiVersioning.*` 명시 (health/social 제외)
- [ ] `@PreAuthorize` 누락 없는지 확인
- [ ] `@Operation(summary=...)` 작성

## 4. 테스트
- [ ] Enum 변경 시: `pnpm nx test domain-core`
- [ ] 유틸리티/보안 변경 시: `./gradlew :libs:backend:global-core:test`
- [ ] 도메인 로직 변경 시: `./gradlew :libs:backend:domain-core:test`
- [ ] 새 기능 추가 시: 대응하는 순수 단위 테스트 작성 (JUnit5 + Mockito + AssertJ)
- [ ] 테스트 클래스: package-private, `@SpringBootTest` 금지
- [ ] 테스트 메서드명: `{메서드}_{시나리오}_{기대결과}` 패턴
- [ ] 전체 테스트 통과 확인: `./gradlew test`

## 5. 커밋 메시지
- 형식: `type(scope): 한국어 요약`
- 코드/문서 수정 완료 답변에 커밋 메시지 1줄 포함
- `printf '%s' '커밋 메시지' | pbcopy` 로 클립보드 복사
