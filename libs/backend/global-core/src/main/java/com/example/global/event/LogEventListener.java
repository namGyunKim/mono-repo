package com.example.global.event;

import com.example.global.exception.support.ExceptionLogTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 로그 이벤트 리스너
@Slf4j
@Component
public class LogEventListener {

    private static final String TX_STATUS_COMMITTED = "COMMITTED";
    private static final String TX_STATUS_ROLLED_BACK = "ROLLED_BACK";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLogEventCommitted(final LogEvent logEvent) {
        logSimpleEvent(logEvent, TX_STATUS_COMMITTED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onLogEventRolledBack(final LogEvent logEvent) {
        logSimpleEvent(logEvent, TX_STATUS_ROLLED_BACK);
    }

    private void logSimpleEvent(final LogEvent logEvent, final String txStatus) {
        if (logEvent == null) {
            return;
        }
        final String resolvedTxStatus = resolveTxStatus(txStatus);
        log.info(
                ExceptionLogTemplates.LOG_EVENT_TEMPLATE.stripTrailing(),
                logEvent.traceId(),
                resolvedTxStatus,
                logEvent.message()
        );
    }

    private String resolveTxStatus(final String txStatus) {
        if (txStatus == null || txStatus.isBlank()) {
            return TX_STATUS_COMMITTED;
        }
        return txStatus;
    }
}
