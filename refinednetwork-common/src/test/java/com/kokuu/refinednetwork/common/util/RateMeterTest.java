package com.kokuu.refinednetwork.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateMeterTest {
    @Test
    void preservesFractionalPerSecondRates() {
        final RateMeter meter = new RateMeter();
        long total = 0;
        for (int tick = 0; tick < 20; tick++) {
            total += meter.next(7);
        }
        assertEquals(7, total);
    }

    @Test
    void rejectsNegativeRates() {
        final RateMeter meter = new RateMeter();
        assertEquals(0, meter.next(-1));
        assertEquals(0, meter.next(0));
    }
}
