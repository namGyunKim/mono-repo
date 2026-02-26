package com.example.global.event;

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
    public void onLogEventCommitted(LogEvent logEvent) {
        logSimpleEvent(logEvent, TX_STATUS_COMMITTED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onLogEventRolledBack(LogEvent logEvent) {
        logSimpleEvent(logEvent, TX_STATUS_ROLLED_BACK);
    }

    private void logSimpleEvent(LogEvent logEvent, String txStatus) {
        if (logEvent == null) {
            return;
        }
        String resolvedTxStatus = resolveTxStatus(txStatus);
        log.info("traceId={}, txStatus={}, {}", logEvent.traceId(), resolvedTxStatus, logEvent.message());
    }

    private String resolveTxStatus(String txStatus) {
        if (txStatus == null || txStatus.isBlank()) {
            return TX_STATUS_COMMITTED;
        }
        return txStatus;
    }
}
