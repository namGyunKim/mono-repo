package com.example.domain.log.service.command;

import com.example.domain.log.event.MemberActivityEvent;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.payload.dto.MemberActivityPayload;
import com.example.domain.log.support.LogDetailsFormatter;
import com.example.global.logging.ClientIpResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 도메인 이벤트(로그) 발행을 위한 공통 Publisher
 * <p>
 * [왜 필요한가]
 * - 여러 서비스/핸들러에서 동일한 형태로 이벤트 발행 코드가 반복되기 쉬움
 * - Client IP 추출 로직(ClientIpExtractor.extract)도 함께 반복되므로 한 곳에서 일관 처리
 */
@Component
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final ClientIpResolver clientIpResolver;

    public void publishMemberActivity(MemberActivityCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command는 필수입니다.");
        }

        final String safeLoginId = resolveLoginId(command.loginId());
        final String safeMemberId = resolveMemberId(command.memberId());
        final String target = "loginId=%s, memberId=%s".formatted(safeLoginId, safeMemberId);
        final String formattedDetails = LogDetailsFormatter.format(command.logType(), target, command.details());

        final MemberActivityPayload payload = MemberActivityPayload.of(
                safeLoginId,
                command.memberId(),
                command.logType(),
                formattedDetails,
                clientIpResolver.resolveClientIp()
        );

        eventPublisher.publishEvent(MemberActivityEvent.from(payload));
    }

    private String resolveLoginId(String loginId) {
        return (loginId == null || loginId.isBlank()) ? "UNKNOWN" : loginId;
    }

    private String resolveMemberId(Long memberId) {
        return (memberId == null) ? "UNKNOWN" : String.valueOf(memberId);
    }
}
