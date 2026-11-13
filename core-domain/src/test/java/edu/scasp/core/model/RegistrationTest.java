package edu.scasp.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RegistrationTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T1 = Instant.parse("2026-02-01T00:00:00Z");

    @Test
    void equalsAndHashCode_ignoreStatusAndTimestamp() {
        Registration a = new Registration("S1", "BCS101", RegistrationStatus.ACTIVE, T0);
        Registration b = new Registration("S1", "bcs101", RegistrationStatus.WITHDRAWN, T1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentStudentOrCourse_isNotEqual() {
        Registration a = new Registration("S1", "BCS101", RegistrationStatus.ACTIVE, T0);
        assertNotEquals(a, new Registration("S2", "BCS101", RegistrationStatus.ACTIVE, T0));
        assertNotEquals(a, new Registration("S1", "BCS102", RegistrationStatus.ACTIVE, T0));
    }

    @Test
    void aSet_cannotHoldTheSameStudentAndCourseTwice() {
        Set<Registration> set = new HashSet<>();
        assertTrue(set.add(new Registration("S1", "BCS101", RegistrationStatus.ACTIVE, T0)));
        assertFalse(set.add(new Registration("S1", "BCS101", RegistrationStatus.ACTIVE, T1)));
        assertEquals(1, set.size());
    }

    @Test
    void withStatus_returnsACopy() {
        Registration active = new Registration("S1", "BCS101", RegistrationStatus.ACTIVE, T0);
        Registration withdrawn = active.withStatus(RegistrationStatus.WITHDRAWN);
        assertEquals(RegistrationStatus.ACTIVE, active.status());
        assertEquals(RegistrationStatus.WITHDRAWN, withdrawn.status());
    }
}
