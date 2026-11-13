package edu.scasp.core.model;

import java.util.Objects;

/** A student profile linked to a User account. Identity is the studentId; natural order is by studentId. */
public record Student(String studentId, String userId, String programme, int yearOfStudy)
        implements Comparable<Student> {

    public Student {
        studentId = Validate.notBlank("studentId", studentId);
        userId = Validate.notBlank("userId", userId);
        programme = Validate.notBlank("programme", programme);
        Validate.positive("yearOfStudy", yearOfStudy);
    }

    @Override
    public int compareTo(Student other) {
        return studentId.compareTo(other.studentId);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Student other && studentId.equals(other.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
