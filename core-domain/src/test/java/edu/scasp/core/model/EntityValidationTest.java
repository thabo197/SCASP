package edu.scasp.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityValidationTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void user_equalsAndHashCodeUseUserId() {
        User a = new User("U1", "Ana", Role.STUDENT, UserStatus.ACTIVE);
        User b = new User(" U1 ", "Changed Name", Role.STUDENT, UserStatus.SUSPENDED);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new User("U2", "Ana", Role.STUDENT, UserStatus.ACTIVE));
    }

    @Test
    void user_requiresIdNameRoleAndStatus() {
        assertThrows(ValidationException.class, () -> new User("", "Ana", Role.STUDENT, UserStatus.ACTIVE));
        assertThrows(ValidationException.class, () -> new User("U1", " ", Role.STUDENT, UserStatus.ACTIVE));
        assertThrows(ValidationException.class, () -> new User("U1", "Ana", null, UserStatus.ACTIVE));
        assertThrows(ValidationException.class, () -> new User("U1", "Ana", Role.STUDENT, null));
    }

    @Test
    void user_withStatusReturnsACopy() {
        User active = new User("U1", "Ana", Role.STUDENT, UserStatus.ACTIVE);
        User suspended = active.withStatus(UserStatus.SUSPENDED);
        assertTrue(active.isActive());
        assertFalse(suspended.isActive());
    }

    @Test
    void student_validatesFieldsAndSortsById() {
        assertThrows(ValidationException.class, () -> new Student("S1", "U1", "BSc", 0));
        assertThrows(ValidationException.class, () -> new Student("S1", "", "BSc", 1));
        List<Student> students = new ArrayList<>(List.of(new Student("S2", "U2", "BSc", 1),
                new Student("S1", "U1", "BSc", 1)));
        Collections.sort(students);
        assertEquals("S1", students.get(0).studentId());
    }

    @Test
    void student_equalsAndHashCodeUseStudentId() {
        Student a = new Student("S1", "U1", "BSc", 1);
        Student b = new Student("S1", "U9", "MSc", 3);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void waitlistEntry_equalsAndHashCodeUseStudentAndCourse() {
        WaitlistEntry a = new WaitlistEntry("S1", "bcs101", T0, 0);
        WaitlistEntry b = new WaitlistEntry("S1", "BCS101", T0.plusSeconds(60), 5);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new WaitlistEntry("S2", "BCS101", T0, 0));
    }

    @Test
    void campusService_rejectsOutOfRangeCoordinates() {
        assertThrows(ValidationException.class, () -> new CampusService("C1", "Library", "Books", 91, 0));
        assertThrows(ValidationException.class, () -> new CampusService("C1", "Library", "Books", 0, -181));
        assertThrows(ValidationException.class, () -> new CampusService("C1", "Library", "Books", Double.NaN, 0));
        assertEquals("Library", new CampusService("C1", "Library", "Books", -15.4, 28.3).name());
    }

    @Test
    void announcementAndAuditEvent_requireAllFields() {
        assertThrows(ValidationException.class, () -> new Announcement("A1", "", "body", Audience.ALL, T0));
        assertThrows(ValidationException.class, () -> new Announcement("A1", "t", "body", null, T0));
        assertThrows(ValidationException.class, () -> new AuditEvent("E1", "", "ACT", "Course", "C1", T0));
        assertEquals(new AuditEvent("E1", "a", "ACT", "Course", "C1", T0),
                new AuditEvent("E1", "b", "OTHER", "Student", "S1", T0.plusSeconds(1)));
    }

    @Test
    void audience_decidesWhichRolesAreReached() {
        assertTrue(Audience.ALL.includes(Role.STUDENT));
        assertTrue(Audience.STUDENTS.includes(Role.STUDENT));
        assertFalse(Audience.STUDENTS.includes(Role.STAFF));
        assertTrue(Audience.STAFF.includes(Role.ADMIN));
        assertFalse(Audience.STAFF.includes(Role.STUDENT));
    }
}
