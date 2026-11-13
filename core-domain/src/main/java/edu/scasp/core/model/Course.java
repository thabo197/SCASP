package edu.scasp.core.model;

import java.util.Locale;
import java.util.Objects;

/**
 * A course. Immutable: changes return a new Course. Identity and natural order are the course code, which is
 * trimmed and upper-cased so "bcs101" and "BCS101" are the same course.
 */
public record Course(String courseCode, String title, int capacity, CourseStatus status)
        implements Comparable<Course> {

    public Course {
        courseCode = normalizeCode(courseCode);
        title = Validate.notBlank("title", title);
        Validate.positive("capacity", capacity);
        Validate.notNull("status", status);
    }

    public static String normalizeCode(String code) {
        return Validate.notBlank("courseCode", code).toUpperCase(Locale.ROOT);
    }

    public Course withTitle(String newTitle) {
        return new Course(courseCode, newTitle, capacity, status);
    }

    public Course withCapacity(int newCapacity) {
        return new Course(courseCode, title, newCapacity, status);
    }

    public Course withStatus(CourseStatus newStatus) {
        return new Course(courseCode, title, capacity, newStatus);
    }

    @Override
    public int compareTo(Course other) {
        return courseCode.compareTo(other.courseCode);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Course other && courseCode.equals(other.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }
}
