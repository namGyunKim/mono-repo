package com.example.domain.member.service.command;

import com.example.domain.member.support.MemberActivityPublishPort;
import com.example.domain.member.support.MemberUniquenessSupport;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * AbstractMemberCommandService.updateMemberCommon 호출 시 필요한
 * 서비스 의존성과 정책 옵션을 묶는 컨텍스트 DTO
 */
public record MemberUpdateContext(
        MemberUniquenessSupport memberUniquenessSupport,
        PasswordEncoder passwordEncoder,
        MemberActivityPublishPort memberActivityPublishPort,
        boolean allowPasswordChange,
        String passwordChangeMessage,
        String updateMessage
) {
    public static MemberUpdateContext of(
            MemberUniquenessSupport memberUniquenessSupport,
            PasswordEncoder passwordEncoder,
            MemberActivityPublishPort memberActivityPublishPort,
            boolean allowPasswordChange,
            String passwordChangeMessage,
            String updateMessage
    ) {
        return new MemberUpdateContext(
                memberUniquenessSupport,
                passwordEncoder,
                memberActivityPublishPort,
                allowPasswordChange,
                passwordChangeMessage,
                updateMessage
        );
    }
}
