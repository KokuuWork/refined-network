package com.kokuu.rswirelesscrafting.common.smartsorter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SorterMatchModeTest {
    @Test
    void loadsNamesAndCyclesWithoutInvalidStates() {
        assertEquals(SorterMatchMode.TAG, SorterMatchMode.byName("tag").orElseThrow());
        assertTrue(SorterMatchMode.byName("not-a-mode").isEmpty());
        assertEquals(SorterMatchMode.TAG, SorterMatchMode.EXACT.next());
        assertEquals(SorterMatchMode.EXACT, SorterMatchMode.TAG.next());
    }
}
