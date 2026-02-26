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

    /**
     * 회원 활동 이벤트 리스너
     * 메인 트랜잭션과 분리하여 저장하거나, 비동기로 처리하여 성능 영향을 최소화합니다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleMemberActivityEvent(MemberActivityEvent event) {
        try {
            final MemberLogCreateCommand command = MemberLogCreateCommand.from(event);
            final MemberLog logEntity = MemberLog.from(command);

            // save 호출 시 JpaAuditing에 의해 createdBy(수행자)가 자동으로 채워짐
            // (AsyncConfig의 TaskDecorator 덕분에 비동기 스레드에서도 보안 컨텍스트 정보 전파 가능)
            memberLogRepository.save(logEntity);

            // 로그 출력 시에는 createdBy를 아직 알 수 없으므로(save 전/후 flush 필요)
            // 로그인 ID가 필요하면 MemberGuard 등 Guard 메서드로 조회하거나, 단순히 "Recorded" 등으로 남김
            log.info(
                    "traceId={}, 회원 활동 로그 저장 완료: loginId={}, logType={}",
                    TraceIdUtils.resolveTraceId(),
                    command.loginId(),
                    command.logType()
            );
        } catch (Exception e) {
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
    }

    private String resolveErrorCode(Exception exception) {
        if (exception instanceof BaseAppException baseAppException
                && baseAppException.getErrorCode() != null) {
            return baseAppException.getErrorCode().getCode();
        }
        return ErrorCode.FAILED.getCode();
    }
}
