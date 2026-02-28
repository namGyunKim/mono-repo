package com.example.domain.member.payload.dto;

import com.example.domain.log.service.command.ActivityEventPublisher;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberSocialCleanupPort;

/**
 * AbstractMemberCommandService.deactivateMemberCommon 호출 시 필요한
 * 서비스 의존성과 정책 옵션을 묶는 컨텍스트 DTO
 */
public record MemberDeactivateContext(
        MemberImageStoragePort memberImageStoragePort,
        MemberSocialCleanupPort memberSocialCleanupPort,
        ActivityEventPublisher activityEventPublisher,
        String inactiveMessage
) {
    public static MemberDeactivateContext of(
            MemberImageStoragePort memberImageStoragePort,
            MemberSocialCleanupPort memberSocialCleanupPort,
            ActivityEventPublisher activityEventPublisher,
            String inactiveMessage
    ) {
        return new MemberDeactivateContext(
                memberImageStoragePort,
                memberSocialCleanupPort,
                activityEventPublisher,
                inactiveMessage
        );
    }
}
