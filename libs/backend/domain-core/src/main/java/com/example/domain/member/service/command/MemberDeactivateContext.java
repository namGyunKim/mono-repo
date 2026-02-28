package com.example.domain.member.service.command;

import com.example.domain.member.support.MemberActivityPublishPort;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberSocialCleanupPort;

/**
 * AbstractMemberCommandService.deactivateMemberCommon 호출 시 필요한
 * 서비스 의존성과 정책 옵션을 묶는 컨텍스트 DTO
 */
public record MemberDeactivateContext(
        MemberImageStoragePort memberImageStoragePort,
        MemberSocialCleanupPort memberSocialCleanupPort,
        MemberActivityPublishPort memberActivityPublishPort,
        String inactiveMessage
) {
    public static MemberDeactivateContext of(
            MemberImageStoragePort memberImageStoragePort,
            MemberSocialCleanupPort memberSocialCleanupPort,
            MemberActivityPublishPort memberActivityPublishPort,
            String inactiveMessage
    ) {
        return new MemberDeactivateContext(
                memberImageStoragePort,
                memberSocialCleanupPort,
                memberActivityPublishPort,
                inactiveMessage
        );
    }
}
