package edu.scasp.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class ScaspInfoTest {

    @Test
    void banner_containsShortAndFullName() {
        assertEquals("SCASP - Smart Campus Academic Services Platform", ScaspInfo.banner());
    }

    @Test
    void banner_isNotBlank() {
        assertFalse(ScaspInfo.banner().isBlank());
    }
}
