package com.example.global.exception.support;

import com.example.global.event.ExceptionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(ExceptionEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
