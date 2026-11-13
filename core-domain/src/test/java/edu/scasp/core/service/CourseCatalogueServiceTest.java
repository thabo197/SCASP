package edu.scasp.core.service;

import static edu.scasp.core.support.Fixture.STAFF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.DuplicateEntityException;
import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.exception.UnauthorizedOperationException;
import edu.scasp.core.exception.ValidationException;
import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseComparators;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.support.Fixture;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseCatalogueServiceTest {

    private static List<String> codes(List<Course> courses) {
        return courses.stream().map(Course::courseCode).toList();
    }

    @Test
    void staffCanCreateACourse_anditStartsOpen() {
        Fixture f = new Fixture(0);
        Course course = f.platform.courses().createCourse(STAFF, "bcs101", "Intro", 30);
        assertEquals("BCS101", course.courseCode());
        assertEquals(CourseStatus.OPEN, course.status());
        assertTrue(f.platform.courses().findCourse("BCS101").isPresent());
    }

    @Test
    void aStudentCannotCreateACourse() {
        Fixture f = new Fixture(1);
        assertThrows(UnauthorizedOperationException.class, () -> f.platform.courses().createCourse("U1", "C1", "Intro", 3));
        assertTrue(f.platform.courses().listCourses().isEmpty());
    }

    @Test
    void duplicateCourseCode_isRejected_evenWithDifferentCase() {
        Fixture f = new Fixture(0);
        f.platform.courses().createCourse(STAFF, "C1", "Intro", 3);
        assertThrows(DuplicateEntityException.class, () -> f.platform.courses().createCourse(STAFF, "c1", "Other", 5));
        assertEquals(1, f.platform.courses().listCourses().size());
    }

    @Test
    void invalidInput_isRejected() {
        Fixture f = new Fixture(0);
        assertThrows(ValidationException.class, () -> f.platform.courses().createCourse(STAFF, "C1", "Intro", 0));
        assertThrows(ValidationException.class, () -> f.platform.courses().createCourse(STAFF, " ", "Intro", 3));
        assertThrows(ValidationException.class, () -> f.platform.courses().createCourse(STAFF, "C1", "", 3));
    }

    @Test
    void updateCourse_changesTitleAndCapacity() {
        Fixture f = new Fixture(0).course("C1", 3);
        Course updated = f.platform.courses().updateCourse(STAFF, "c1", "New title", 5);
        assertEquals("New title", updated.title());
        assertEquals(5, f.platform.courses().findCourse("C1").orElseThrow().capacity());
    }

    @Test
    void updateCourse_cannotReduceCapacityBelowTheRegisteredStudents() {
        Fixture f = new Fixture(3).course("C1", 3);
        f.register("S1", "C1");
        f.register("S2", "C1");
        f.register("S3", "C1");
        assertThrows(InvalidOperationException.class, () -> f.platform.courses().updateCourse(STAFF, "C1", "T", 2));
        assertEquals(3, f.platform.courses().findCourse("C1").orElseThrow().capacity());
        assertEquals(3, f.platform.courses().updateCourse(STAFF, "C1", "T", 3).capacity());
    }

    @Test
    void updateCourse_unknownCourse_isNotFound() {
        Fixture f = new Fixture(0);
        assertThrows(EntityNotFoundException.class, () -> f.platform.courses().updateCourse(STAFF, "NOPE", "T", 3));
    }

    @Test
    void increasingCapacity_promotesWaitingStudents() {
        Fixture f = new Fixture(4).course("C1", 1);
        for (String s : List.of("S1", "S2", "S3", "S4")) {
            f.register(s, "C1");
        }
        f.platform.courses().updateCourse(STAFF, "C1", "T", 3);
        assertEquals(3, f.platform.registrations().registrationsForCourse("C1").size());
        assertEquals(List.of("S4"),
                f.platform.registrations().waitlistFor("C1").stream().map(WaitlistEntry::studentId).toList());
    }

    @Test
    void invalidStatusChange_isRejected() {
        Fixture f = new Fixture(0).course("C1", 3);
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.CANCELLED);
        assertThrows(InvalidOperationException.class,
                () -> f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.OPEN));
        assertThrows(InvalidOperationException.class,
                () -> new Fixture(0).course("C2", 1).platform.courses().changeStatus(STAFF, "C2", CourseStatus.OPEN));
    }

    @Test
    void reopeningACourse_promotesWaitingStudents() {
        Fixture f = new Fixture(3).course("C1", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.CLOSED);
        f.platform.registrations().withdraw("U1", "S1", "C1");
        assertEquals(0, f.platform.registrations().registrationsForCourse("C1").size());
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.OPEN);
        assertEquals(List.of("S2"),
                f.platform.registrations().registrationsForCourse("C1").stream().map(r -> r.studentId()).toList());
    }

    @Test
    void search_matchesCodeOrTitle_ignoringCase() {
        Fixture f = new Fixture(0);
        f.platform.courses().createCourse(STAFF, "BCS101", "Introduction to Programming", 3);
        f.platform.courses().createCourse(STAFF, "BCS201", "Databases", 3);
        f.platform.courses().createCourse(STAFF, "MAT101", "Calculus", 3);
        assertEquals(List.of("BCS101"), codes(f.platform.courses().search("programming")));
        assertEquals(List.of("BCS101", "BCS201"), codes(f.platform.courses().search("bcs")));
        assertEquals(List.of("MAT101"), codes(f.platform.courses().search("calc")));
        assertTrue(f.platform.courses().search("zzz").isEmpty());
    }

    @Test
    void blankSearch_returnsEveryCourse() {
        Fixture f = new Fixture(0).course("C1", 1).course("C2", 1);
        assertEquals(2, f.platform.courses().search("  ").size());
        assertEquals(2, f.platform.courses().search(null).size());
    }

    @Test
    void listCourses_usesNaturalOrderOrAComparator() {
        Fixture f = new Fixture(0);
        f.platform.courses().createCourse(STAFF, "C2", "Zebra", 10);
        f.platform.courses().createCourse(STAFF, "C3", "Apple", 30);
        f.platform.courses().createCourse(STAFF, "C1", "Mango", 20);
        assertEquals(List.of("C1", "C2", "C3"), codes(f.platform.courses().listCourses()));
        assertEquals(List.of("C3", "C1", "C2"), codes(f.platform.courses().listCourses(CourseComparators.BY_TITLE)));
        assertEquals(List.of("C3", "C1", "C2"),
                codes(f.platform.courses().listCourses(CourseComparators.BY_CAPACITY_DESC)));
    }

    @Test
    void findByStatus_filtersCourses() {
        Fixture f = new Fixture(0).course("C1", 1).course("C2", 1);
        f.platform.courses().changeStatus(STAFF, "C2", CourseStatus.CLOSED);
        assertEquals(List.of("C1"), codes(f.platform.courses().findByStatus(CourseStatus.OPEN)));
        assertEquals(List.of("C2"), codes(f.platform.courses().findByStatus(CourseStatus.CLOSED)));
    }

    @Test
    void listResults_areUnmodifiable() {
        Fixture f = new Fixture(0).course("C1", 1);
        assertThrows(UnsupportedOperationException.class, () -> f.platform.courses().listCourses().clear());
        assertThrows(UnsupportedOperationException.class, () -> f.platform.courses().search("c").clear());
        assertEquals(1, f.platform.courses().listCourses().size());
    }

    @Test
    void courseChanges_areAudited() {
        Fixture f = new Fixture(0);
        f.platform.courses().createCourse(STAFF, "C1", "Intro", 3);
        f.platform.courses().updateCourse(STAFF, "C1", "Intro 2", 4);
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.CLOSED);
        assertEquals(List.of("CREATE_COURSE", "UPDATE_COURSE", "CHANGE_COURSE_STATUS"),
                f.platform.auditTrail().eventsFor("Course", "C1").stream().map(e -> e.action()).toList());
    }
}
