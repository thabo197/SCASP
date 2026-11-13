package edu.scasp.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.ValidationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseTest {

    private static Course course(String code, String title, int capacity, CourseStatus status) {
        return new Course(code, title, capacity, status);
    }

    @Test
    void courseCode_isTrimmedAndUpperCased() {
        assertEquals("BCS101", course("  bcs101 ", "Intro", 3, CourseStatus.OPEN).courseCode());
    }

    @Test
    void blankCode_isRejected() {
        assertThrows(ValidationException.class, () -> course("  ", "Intro", 3, CourseStatus.OPEN));
        assertThrows(ValidationException.class, () -> course(null, "Intro", 3, CourseStatus.OPEN));
    }

    @Test
    void blankTitle_isRejected() {
        assertThrows(ValidationException.class, () -> course("C1", " ", 3, CourseStatus.OPEN));
    }

    @Test
    void nonPositiveCapacity_isRejected() {
        assertThrows(ValidationException.class, () -> course("C1", "Intro", 0, CourseStatus.OPEN));
        assertThrows(ValidationException.class, () -> course("C1", "Intro", -5, CourseStatus.OPEN));
    }

    @Test
    void nullStatus_isRejected() {
        assertThrows(ValidationException.class, () -> course("C1", "Intro", 3, null));
    }

    @Test
    void equalsAndHashCode_useTheCourseCodeOnly() {
        Course a = course("BCS101", "Intro", 3, CourseStatus.OPEN);
        Course sameCode = course("bcs101", "Different title", 9, CourseStatus.CLOSED);
        Course other = course("BCS102", "Intro", 3, CourseStatus.OPEN);
        assertEquals(a, sameCode);
        assertEquals(a.hashCode(), sameCode.hashCode());
        assertNotEquals(a, other);
    }

    @Test
    void changes_returnNewObjectsAndLeaveTheOriginalUnchanged() {
        Course original = course("C1", "Intro", 3, CourseStatus.OPEN);
        Course changed = original.withCapacity(10).withTitle("New").withStatus(CourseStatus.CLOSED);
        assertEquals(3, original.capacity());
        assertEquals("Intro", original.title());
        assertEquals(CourseStatus.OPEN, original.status());
        assertEquals(10, changed.capacity());
        assertEquals("New", changed.title());
        assertEquals(CourseStatus.CLOSED, changed.status());
    }

    @Test
    void naturalOrder_isByCourseCode() {
        List<Course> courses = new ArrayList<>(List.of(
                course("C3", "x", 1, CourseStatus.OPEN),
                course("C1", "x", 1, CourseStatus.OPEN),
                course("C2", "x", 1, CourseStatus.OPEN)));
        Collections.sort(courses);
        assertEquals(List.of("C1", "C2", "C3"), courses.stream().map(Course::courseCode).toList());
    }

    @Test
    void comparators_orderByTitleCapacityAndStatus() {
        Course a = course("C1", "banana", 5, CourseStatus.CLOSED);
        Course b = course("C2", "Apple", 9, CourseStatus.OPEN);
        Course c = course("C3", "cherry", 5, CourseStatus.OPEN);
        List<Course> all = List.of(a, b, c);

        assertEquals(List.of("C2", "C1", "C3"),
                all.stream().sorted(CourseComparators.BY_TITLE).map(Course::courseCode).toList());
        assertEquals(List.of("C2", "C1", "C3"),
                all.stream().sorted(CourseComparators.BY_CAPACITY_DESC).map(Course::courseCode).toList());
        assertEquals(List.of("C2", "C3", "C1"),
                all.stream().sorted(CourseComparators.BY_STATUS_THEN_CODE).map(Course::courseCode).toList());
    }

    @Test
    void statusTransitions_followTheAllowedRules() {
        assertTrue(CourseStatus.OPEN.canTransitionTo(CourseStatus.CLOSED));
        assertTrue(CourseStatus.CLOSED.canTransitionTo(CourseStatus.OPEN));
        assertTrue(CourseStatus.OPEN.canTransitionTo(CourseStatus.CANCELLED));
        assertFalse(CourseStatus.CANCELLED.canTransitionTo(CourseStatus.OPEN));
        assertFalse(CourseStatus.OPEN.canTransitionTo(CourseStatus.OPEN));
    }
}
