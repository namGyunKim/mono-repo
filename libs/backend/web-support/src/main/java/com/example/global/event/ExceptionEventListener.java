package com.example.global.event;

import com.example.domain.log.event.ExceptionEvent;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogTemplates;
import com.example.global.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class ExceptionEventListener {

    private static final String TX_STATUS_COMMITTED = "COMMITTED";
    private static final String TX_STATUS_ROLLED_BACK = "ROLLED_BACK";

    private final ObjectMapper objectMapper;
    private final boolean structuredExceptionLoggingEnabled;

    public ExceptionEventListener(
            ObjectMapper objectMapper,
            @Value("${app.logging.exception.structured-enabled:false}") boolean structuredExceptionLoggingEnabled
    ) {
        this.objectMapper = objectMapper;
        this.structuredExceptionLoggingEnabled = structuredExceptionLoggingEnabled;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onExceptionEventCommitted(ExceptionEvent exceptionEvent) {
        logExceptionEvent(exceptionEvent, TX_STATUS_COMMITTED);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onExceptionEventRolledBack(ExceptionEvent exceptionEvent) {
        logExceptionEvent(exceptionEvent, TX_STATUS_ROLLED_BACK);
    }

    private void logExceptionEvent(ExceptionEvent exceptionEvent, String txStatus) {
        if (exceptionEvent == null) {
            return;
        }
        String resolvedTxStatus = resolveTxStatus(txStatus);

        log.error(
                ExceptionLogTemplates.EXCEPTION_EVENT_LOG_TEMPLATE.stripTrailing(),
                exceptionEvent.traceId(),
                resolvedTxStatus,
                exceptionEvent.toLogString()
        );

        if (structuredExceptionLoggingEnabled) {
            String structuredPayload = toStructuredPayload(exceptionEvent, resolvedTxStatus);
            if (structuredPayload != null) {
                log.error(
                        ExceptionLogTemplates.EXCEPTION_EVENT_STRUCTURED_LOG_TEMPLATE.stripTrailing(),
                        exceptionEvent.traceId(),
                        resolvedTxStatus,
                        structuredPayload
                );
            }
        }
    }

    private String toStructuredPayload(ExceptionEvent exceptionEvent, String txStatus) {
        try {
            java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>(exceptionEvent.getStructuredLog());
            payload.put("txStatus", txStatus);
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            log.warn(
                    ExceptionLogTemplates.EXCEPTION_EVENT_STRUCTURED_FAIL_LOG_TEMPLATE.stripTrailing(),
                    resolveTraceId(exceptionEvent),
                    txStatus,
                    ErrorCode.FAILED.getCode(),
                    e.getClass().getSimpleName(),
                    e.getMessage() != null ? e.getMessage() : "UNKNOWN"
            );
            return null;
        }
    }

    private String resolveTxStatus(String txStatus) {
        if (txStatus == null || txStatus.isBlank()) {
            return TX_STATUS_COMMITTED;
        }
        return txStatus;
    }

    private String resolveTraceId(ExceptionEvent exceptionEvent) {
        if (exceptionEvent == null || exceptionEvent.traceId() == null || exceptionEvent.traceId().isBlank()) {
            return TraceIdUtils.resolveTraceId();
        }
        return exceptionEvent.traceId();
    }
}
