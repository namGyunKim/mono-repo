package com.example.global.config.async;

import com.example.global.security.SecurityContextManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContext;

import java.util.Map;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

    private final SecurityContextManager securityContextManager;

    /**
     * 공통 비동기 처리를 위한 Executor 설정
     * - Virtual Thread 사용으로 높은 처리량 보장
     * - MDC(TraceId) 및 SecurityContext(인증 정보) 전파 기능 포함
     * - @Primary를 사용하여 별도의 이름 지정 없이 @Async를 사용할 때 기본으로 적용됨
     */
    @Bean(name = {"taskExecutor", "emailTaskExecutor"})
    @Primary
    public AsyncTaskExecutor taskExecutor() {
        final TaskExecutorAdapter adapter = new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
        adapter.setTaskDecorator(this::decorateWithContext);
        return adapter;
    }

    private Runnable decorateWithContext(final Runnable runnable) {
        final Map<String, String> contextMap = MDC.getCopyOfContextMap();
        final SecurityContext securityContext = securityContextManager.getContext();

        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                if (securityContext != null) {
                    securityContextManager.setContext(securityContext);
                }
                runnable.run();
            } finally {
                MDC.clear();
                securityContextManager.clearContext();
            }
        };
    }
}
