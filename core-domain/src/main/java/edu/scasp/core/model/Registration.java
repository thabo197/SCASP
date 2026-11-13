package edu.scasp.core.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A student's seat in a course. Identity is the pair (studentId, courseCode) only, so a Set can never hold two
 * registrations for the same student and course, whatever their status or timestamp (invariant I-01).
 */
public record Registration(String studentId, String courseCode, RegistrationStatus status, Instant createdAt) {

    public Registration {
        studentId = Validate.notBlank("studentId", studentId);
        courseCode = Course.normalizeCode(courseCode);
        Validate.notNull("status", status);
        Validate.notNull("createdAt", createdAt);
    }

    public Registration withStatus(RegistrationStatus newStatus) {
        return new Registration(studentId, courseCode, newStatus, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Registration other
                && studentId.equals(other.studentId) && courseCode.equals(other.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseCode);
    }
}
