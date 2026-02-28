package com.example.domain.social.google.support;

import java.util.concurrent.TimeUnit;

final class GoogleOauthTimingSupport {

    private GoogleOauthTimingSupport() {
    }

    static long elapsedMs(long startNanos) {
        return startNanos > 0 ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos) : 0L;
    }
}
