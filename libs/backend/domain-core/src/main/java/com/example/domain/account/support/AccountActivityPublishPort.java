package com.example.domain.account.support;

import com.example.domain.account.payload.dto.AccountActivityPublishCommand;

/**
 * account -> log 도메인 경계를 넘는 활동 로그 발행 포트
 * <p>
 * AccountCommandService가 LogActivityPublisher / MemberActivityCommand에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface AccountActivityPublishPort {

    void publishMemberActivity(AccountActivityPublishCommand command);
}
