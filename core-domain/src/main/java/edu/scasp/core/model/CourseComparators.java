package edu.scasp.core.model;

import java.util.Comparator;

/** Alternative orderings for courses, in addition to the natural order (by course code). */
public final class CourseComparators {

    public static final Comparator<Course> BY_CODE = Comparator.naturalOrder();

    public static final Comparator<Course> BY_TITLE =
            Comparator.<Course, String>comparing(Course::title, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Course::courseCode);

    public static final Comparator<Course> BY_CAPACITY_DESC =
            Comparator.<Course>comparingInt(Course::capacity).reversed().thenComparing(Course::courseCode);

    public static final Comparator<Course> BY_STATUS_THEN_CODE =
            Comparator.<Course, CourseStatus>comparing(Course::status).thenComparing(Course::courseCode);

    private CourseComparators() {
    }
}
