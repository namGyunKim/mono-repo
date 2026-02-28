package com.example.domain.member.support;

import com.example.domain.log.enums.LogType;

/**
 * member → log 도메인 경계를 넘는 활동 로그 발행 포트
 * <p>
 * AdminMemberCommandService / UserMemberCommandService / AbstractMemberCommandService가
 * LogActivityPublisher / MemberActivityCommand에 직접 의존하지 않도록 추상화합니다.
 */
public interface MemberActivityPublishPort {

    void publishMemberActivity(String loginId, Long memberId, LogType logType, String details);
}
