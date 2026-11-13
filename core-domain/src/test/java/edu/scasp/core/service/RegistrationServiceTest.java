package edu.scasp.core.service;

import static edu.scasp.core.support.Fixture.ADMIN;
import static edu.scasp.core.support.Fixture.STAFF;
import static edu.scasp.core.support.Fixture.userOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.DuplicateEntityException;
import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.exception.UnauthorizedOperationException;
import edu.scasp.core.model.AuditEvent;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Registration;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.support.Fixture;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegistrationServiceTest {

    private static List<String> ids(List<Registration> list) {
        return list.stream().map(Registration::studentId).toList();
    }

    private static List<String> waiting(Fixture f, String course) {
        return f.platform.registrations().waitlistFor(course).stream().map(WaitlistEntry::studentId).toList();
    }

    // ---- registering ----

    @Test
    void register_whenASeatIsFree_registersTheStudent() {
        Fixture f = new Fixture(2).course("C1", 2);
        RegistrationOutcome outcome = f.platform.registrations().register("U1", "S1", "C1");
        assertTrue(outcome.isRegistered());
        assertEquals("S1", outcome.registration().studentId());
        assertTrue(f.platform.registrations().isRegistered("S1", "C1"));
        assertEquals(1, f.platform.registrations().availableSeats("C1"));
    }

    @Test
    void register_whenFull_addsStudentsToTheWaitingListInOrder() {
        Fixture f = new Fixture(4).course("C1", 2);
        f.register("S1", "C1");
        f.register("S2", "C1");
        RegistrationOutcome third = f.platform.registrations().register("U3", "S3", "C1");
        RegistrationOutcome fourth = f.platform.registrations().register("U4", "S4", "C1");
        assertEquals(RegistrationOutcome.Status.WAITLISTED, third.status());
        assertEquals(1, third.waitlistPosition());
        assertEquals(2, fourth.waitlistPosition());
        assertEquals(List.of("S3", "S4"), waiting(f, "C1"));
        assertEquals(0, f.platform.registrations().availableSeats("C1"));
    }

    @Test
    void register_neverExceedsCapacity() {
        Fixture f = new Fixture(10).course("C1", 3);
        for (int i = 1; i <= 10; i++) {
            f.register("S" + i, "C1");
        }
        assertEquals(3, f.platform.registrations().registrationsForCourse("C1").size());
        assertEquals(7, waiting(f, "C1").size());
    }

    @Test
    void register_sameStudentTwice_isRejected() {
        Fixture f = new Fixture(1).course("C1", 2);
        f.register("S1", "C1");
        assertThrows(DuplicateEntityException.class, () -> f.register("S1", "C1"));
        assertEquals(1, f.platform.registrations().registrationsForCourse("C1").size());
    }

    @Test
    void register_whileAlreadyWaiting_isRejected() {
        Fixture f = new Fixture(2).course("C1", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        assertThrows(DuplicateEntityException.class, () -> f.register("S2", "C1"));
        assertEquals(List.of("S2"), waiting(f, "C1"));
    }

    @Test
    void register_closedOrCancelledCourse_isRejected() {
        Fixture f = new Fixture(1).course("C1", 2);
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.CLOSED);
        assertThrows(InvalidOperationException.class, () -> f.register("S1", "C1"));
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.CANCELLED);
        assertThrows(InvalidOperationException.class, () -> f.register("S1", "C1"));
    }

    @Test
    void register_unknownStudentOrCourse_isNotFound() {
        Fixture f = new Fixture(1).course("C1", 2);
        assertThrows(EntityNotFoundException.class, () -> f.platform.registrations().register(STAFF, "S99", "C1"));
        assertThrows(EntityNotFoundException.class, () -> f.platform.registrations().register("U1", "S1", "NOPE"));
    }

    @Test
    void register_courseCodeIsCaseInsensitive() {
        Fixture f = new Fixture(1).course("BCS101", 2);
        f.platform.registrations().register("U1", "S1", " bcs101 ");
        assertTrue(f.platform.registrations().isRegistered("S1", "BCS101"));
    }

    // ---- authorisation ----

    @Test
    void studentMayNotActForAnotherStudent() {
        Fixture f = new Fixture(2).course("C1", 2);
        assertThrows(UnauthorizedOperationException.class, () -> f.platform.registrations().register("U2", "S1", "C1"));
        assertFalse(f.platform.registrations().isRegistered("S1", "C1"));
    }

    @Test
    void staffAndAdminMayActForAStudent() {
        Fixture f = new Fixture(2).course("C1", 2);
        f.platform.registrations().register(STAFF, "S1", "C1");
        f.platform.registrations().register(ADMIN, "S2", "C1");
        assertEquals(List.of("S1", "S2"), ids(f.platform.registrations().registrationsForCourse("C1")));
    }

    @Test
    void unknownOrSuspendedActor_isUnauthorized() {
        Fixture f = new Fixture(1).course("C1", 2);
        assertThrows(UnauthorizedOperationException.class, () -> f.platform.registrations().register("GHOST", "S1", "C1"));
        f.suspend("U1");
        assertThrows(UnauthorizedOperationException.class, () -> f.platform.registrations().register("U1", "S1", "C1"));
    }

    @Test
    void staffCannotRegisterAnInactiveStudent() {
        Fixture f = new Fixture(1).course("C1", 2);
        f.suspend("U1");
        assertThrows(InvalidOperationException.class, () -> f.platform.registrations().register(STAFF, "S1", "C1"));
    }

    // ---- withdrawing and promotion ----

    @Test
    void withdraw_promotesTheFirstWaitingStudent() {
        Fixture f = new Fixture(4).course("C1", 2);
        for (String s : List.of("S1", "S2", "S3", "S4")) {
            f.register(s, "C1");
        }
        WithdrawalResult result = f.platform.registrations().withdraw("U1", "S1", "C1");
        assertEquals(WithdrawalResult.Kind.REGISTRATION_WITHDRAWN, result.kind());
        assertEquals(List.of("S3"), ids(result.promoted()));
        assertEquals(List.of("S2", "S3"), ids(f.platform.registrations().registrationsForCourse("C1")));
        assertEquals(List.of("S4"), waiting(f, "C1"));
        assertFalse(f.platform.registrations().isRegistered("S1", "C1"));
    }

    @Test
    void withdraw_skipsAnIneligibleStudentAndKeepsTheirPlace() {
        Fixture f = new Fixture(4).course("C1", 1);
        for (String s : List.of("S1", "S2", "S3", "S4")) {
            f.register(s, "C1");
        }
        f.suspend("U2");
        WithdrawalResult result = f.platform.registrations().withdraw("U1", "S1", "C1");
        assertEquals(List.of("S3"), ids(result.promoted()));
        assertEquals(List.of("S2", "S4"), waiting(f, "C1"));
    }

    @Test
    void withdraw_whenNobodyEligibleIsWaiting_leavesTheSeatFree() {
        Fixture f = new Fixture(2).course("C1", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        f.suspend("U2");
        WithdrawalResult result = f.platform.registrations().withdraw("U1", "S1", "C1");
        assertTrue(result.promoted().isEmpty());
        assertEquals(1, f.platform.registrations().availableSeats("C1"));
        assertEquals(List.of("S2"), waiting(f, "C1"));
    }

    @Test
    void withdraw_fromTheWaitingListOnly_promotesNobody() {
        Fixture f = new Fixture(3).course("C1", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        f.register("S3", "C1");
        WithdrawalResult result = f.platform.registrations().withdraw("U2", "S2", "C1");
        assertEquals(WithdrawalResult.Kind.REMOVED_FROM_WAITLIST, result.kind());
        assertTrue(result.promoted().isEmpty());
        assertEquals(List.of("S3"), waiting(f, "C1"));
        assertTrue(f.platform.registrations().isRegistered("S1", "C1"));
    }

    @Test
    void withdraw_whenNeitherRegisteredNorWaiting_isNotFound() {
        Fixture f = new Fixture(1).course("C1", 1);
        assertThrows(EntityNotFoundException.class, () -> f.platform.registrations().withdraw("U1", "S1", "C1"));
    }

    @Test
    void withdraw_byAnotherStudent_isUnauthorized() {
        Fixture f = new Fixture(2).course("C1", 2);
        f.register("S1", "C1");
        assertThrows(UnauthorizedOperationException.class, () -> f.platform.registrations().withdraw("U2", "S1", "C1"));
        assertTrue(f.platform.registrations().isRegistered("S1", "C1"));
    }

    @Test
    void withdraw_fromAClosedCourse_freesTheSeatWithoutPromoting() {
        Fixture f = new Fixture(3).course("C1", 1);
        for (String s : List.of("S1", "S2")) {
            f.register(s, "C1");
        }
        f.platform.courses().changeStatus(STAFF, "C1", CourseStatus.CLOSED);
        WithdrawalResult result = f.platform.registrations().withdraw("U1", "S1", "C1");
        assertTrue(result.promoted().isEmpty());
        assertEquals(List.of("S2"), waiting(f, "C1"));
    }

    @Test
    void aWithdrawnStudent_canRegisterAgain() {
        Fixture f = new Fixture(1).course("C1", 1);
        f.register("S1", "C1");
        f.platform.registrations().withdraw("U1", "S1", "C1");
        f.register("S1", "C1");
        assertTrue(f.platform.registrations().isRegistered("S1", "C1"));
    }

    // ---- results are defensive copies ----

    @Test
    void queryResults_cannotBeUsedToChangeTheService() {
        Fixture f = new Fixture(2).course("C1", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        assertThrows(UnsupportedOperationException.class,
                () -> f.platform.registrations().registrationsForCourse("C1").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> f.platform.registrations().waitlistFor("C1").clear());
        assertEquals(1, f.platform.registrations().registrationsForCourse("C1").size());
        assertEquals(1, f.platform.registrations().waitlistFor("C1").size());
    }

    @Test
    void registrationsForStudent_listsTheirCoursesInOrder() {
        Fixture f = new Fixture(1).course("C2", 1).course("C1", 1);
        f.register("S1", "C2");
        f.register("S1", "C1");
        assertEquals(List.of("C1", "C2"),
                f.platform.registrations().registrationsForStudent("S1").stream().map(Registration::courseCode).toList());
    }

    // ---- audit trail ----

    @Test
    void everyStateChange_isRecordedWithActorActionAndTime() {
        Fixture f = new Fixture(3).course("C1", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        f.platform.registrations().withdraw("U1", "S1", "C1");

        List<AuditEvent> events = f.platform.auditTrail().events();
        List<String> actions = events.stream().map(AuditEvent::action).toList();
        assertEquals(List.of("REGISTER", "JOIN_WAITLIST", "WITHDRAW", "PROMOTE_FROM_WAITLIST"), actions);
        assertEquals(userOf("S1"), events.get(0).actor());
        assertEquals(AuditTrail.SYSTEM, events.get(3).actor());
        assertEquals("S2:C1", events.get(3).entityId());
        for (int i = 1; i < events.size(); i++) {
            assertTrue(events.get(i).occurredAt().isAfter(events.get(i - 1).occurredAt()));
        }
    }

    @Test
    void auditEvents_cannotBeChangedThroughTheReturnedList() {
        Fixture f = new Fixture(1).course("C1", 1);
        f.register("S1", "C1");
        assertThrows(UnsupportedOperationException.class, () -> f.platform.auditTrail().events().clear());
        assertEquals(1, f.platform.auditTrail().events().size());
        assertEquals(1, f.platform.auditTrail().eventsFor("Registration", "S1:C1").size());
        assertTrue(f.platform.auditTrail().eventsFor("Registration", "S9:C1").isEmpty());
    }

    @Test
    void failedOperations_leaveNoAuditEvent() {
        Fixture f = new Fixture(1).course("C1", 1);
        f.register("S1", "C1");
        int before = f.platform.auditTrail().events().size();
        assertThrows(DuplicateEntityException.class, () -> f.register("S1", "C1"));
        assertEquals(before, f.platform.auditTrail().events().size());
    }
}
