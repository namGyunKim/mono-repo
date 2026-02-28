package com.example.domain.member.payload.dto;

import com.example.domain.log.service.command.ActivityEventPublisher;
import com.example.domain.member.support.MemberUniquenessSupport;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AbstractMemberCommandService.updateMemberCommon 호출 시 필요한
 * 서비스 의존성과 정책 옵션을 묶는 컨텍스트 DTO
 */
public record MemberUpdateContext(
        MemberUniquenessSupport memberUniquenessSupport,
        PasswordEncoder passwordEncoder,
        ActivityEventPublisher activityEventPublisher,
        boolean allowPasswordChange,
        String passwordChangeMessage,
        String updateMessage
) {
    public static MemberUpdateContext of(
            MemberUniquenessSupport memberUniquenessSupport,
            PasswordEncoder passwordEncoder,
            ActivityEventPublisher activityEventPublisher,
            boolean allowPasswordChange,
            String passwordChangeMessage,
            String updateMessage
    ) {
        return new MemberUpdateContext(
                memberUniquenessSupport,
                passwordEncoder,
                activityEventPublisher,
                allowPasswordChange,
                passwordChangeMessage,
                updateMessage
        );
    }
}
