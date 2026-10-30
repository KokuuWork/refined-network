package com.kokuu.refinednetwork.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimiterTest {
    @Test
    void resetsBudgetOnANewGameSecond() {
        final RateLimiter limiter = new RateLimiter(10);
        assertEquals(10, limiter.remaining(0));
        limiter.consume(4, 0);
        assertEquals(6, limiter.remaining(0));
        limiter.consume(6, 19);
        assertEquals(0, limiter.remaining(19));
        assertEquals(10, limiter.remaining(20));
    }

    @Test
    void updatesRateWithoutRecreatingTheLimiter() {
        final RateLimiter limiter = new RateLimiter(10);
        limiter.consume(10, 0);
        limiter.setPerSecond(20);
        assertEquals(10, limiter.remaining(0));
    }

    @Test
    void resetsWhenGameTimeMovesBackwards() {
        final RateLimiter limiter = new RateLimiter(10);
        limiter.consume(10, 100);
        assertEquals(0, limiter.remaining(100));
        assertEquals(10, limiter.remaining(50));
    }
}
