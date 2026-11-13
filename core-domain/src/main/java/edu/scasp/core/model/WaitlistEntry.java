package edu.scasp.core.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A student waiting for a seat. Identity is (studentId, courseCode). Waiting lists are strictly first in, first out
 * in this version, so the priority field is reserved and not used for ordering.
 */
public record WaitlistEntry(String studentId, String courseCode, Instant createdAt, int priority) {

    public WaitlistEntry {
        studentId = Validate.notBlank("studentId", studentId);
        courseCode = Course.normalizeCode(courseCode);
        Validate.notNull("createdAt", createdAt);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WaitlistEntry other
                && studentId.equals(other.studentId) && courseCode.equals(other.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseCode);
    }
}
