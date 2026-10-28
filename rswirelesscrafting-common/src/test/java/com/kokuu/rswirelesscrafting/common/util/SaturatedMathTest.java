package com.kokuu.rswirelesscrafting.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaturatedMathTest {
    @Test
    void saturatesInsteadOfOverflowing() {
        assertEquals(Long.MAX_VALUE, SaturatedMath.add(Long.MAX_VALUE, 1));
        assertEquals(Long.MIN_VALUE, SaturatedMath.add(Long.MIN_VALUE, -1));
        assertEquals(Long.MAX_VALUE, SaturatedMath.multiply(Long.MAX_VALUE, 2));
        assertEquals(Long.MAX_VALUE, SaturatedMath.multiplyPercent(Long.MAX_VALUE, 1600));
    }
}
