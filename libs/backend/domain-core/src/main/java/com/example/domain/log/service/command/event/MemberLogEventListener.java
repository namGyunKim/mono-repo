package com.example.domain.log.service.command.event;

import com.example.domain.log.entity.MemberLog;
import com.example.domain.log.event.MemberActivityEvent;
import com.example.domain.log.payload.dto.MemberLogCreateCommand;
import com.example.domain.log.repository.MemberLogRepository;
import com.example.global.exception.BaseAppException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLogEventListener {

    private final MemberLogRepository memberLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberActivityEvent(MemberActivityEvent event) {
        try {
            final MemberLogCreateCommand command = MemberLogCreateCommand.from(event);
            final MemberLog logEntity = MemberLog.from(command);
            memberLogRepository.save(logEntity);

            log.info(
                    "traceId={}, 회원 활동 로그 저장 완료: loginId={}, logType={}",
                    TraceIdUtils.resolveTraceId(),
                    command.loginId(),
                    command.logType()
            );
        } catch (Exception e) {
            logSaveError(e, event);
        }
    }

    private void logSaveError(Exception e, MemberActivityEvent event) {
        final String errorCode = resolveErrorCode(e);
        log.error(
                "traceId={}, errorCode={}, 로그 저장 중 오류 발생: exceptionName={}, loginId={}, memberId={}, logType={}, message={}",
                TraceIdUtils.resolveTraceId(),
                errorCode,
                e.getClass().getSimpleName(),
                event.loginId(),
                event.memberId(),
                event.logType(),
                e.getMessage(),
                e
        );
    }

    private String resolveErrorCode(Exception exception) {
        if (exception instanceof BaseAppException baseAppException
                && baseAppException.getErrorCode() != null) {
            return baseAppException.getErrorCode().getCode();
        }
        return ErrorCode.FAILED.getCode();
    }
}
