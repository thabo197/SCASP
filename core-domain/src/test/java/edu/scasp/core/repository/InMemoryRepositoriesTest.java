package edu.scasp.core.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Registration;
import edu.scasp.core.model.RegistrationStatus;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.repository.memory.InMemoryCourseRepository;
import edu.scasp.core.repository.memory.InMemoryRegistrationRepository;
import edu.scasp.core.repository.memory.InMemoryWaitlistRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryRepositoriesTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static Registration reg(String student, String course, long seconds) {
        return new Registration(student, course, RegistrationStatus.ACTIVE, T0.plusSeconds(seconds));
    }

    private static WaitlistEntry wait(String student, String course) {
        return new WaitlistEntry(student, course, T0, 0);
    }

    // ---- generic Map-backed repository (courses use a TreeMap) ----

    @Test
    void courseRepository_listsInCourseCodeOrderWhateverTheInsertionOrder() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        repo.save(new Course("C3", "c", 1, CourseStatus.OPEN));
        repo.save(new Course("C1", "a", 1, CourseStatus.OPEN));
        repo.save(new Course("C2", "b", 1, CourseStatus.OPEN));
        assertEquals(List.of("C1", "C2", "C3"), repo.findAll().stream().map(Course::courseCode).toList());
    }

    @Test
    void saveReplacesTheEntityWithTheSameKey_andDeleteRemovesIt() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        repo.save(new Course("C1", "Old", 1, CourseStatus.OPEN));
        repo.save(new Course("C1", "New", 2, CourseStatus.OPEN));
        assertEquals(1, repo.count());
        assertEquals("New", repo.findById("C1").orElseThrow().title());
        assertTrue(repo.existsById("C1"));
        assertTrue(repo.deleteById("C1"));
        assertFalse(repo.deleteById("C1"));
        assertTrue(repo.findById("C1").isEmpty());
    }

    @Test
    void findAll_returnsAnUnmodifiableCopy() {
        InMemoryCourseRepository repo = new InMemoryCourseRepository();
        repo.save(new Course("C1", "a", 1, CourseStatus.OPEN));
        List<Course> snapshot = repo.findAll();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new Course("C2", "b", 1, CourseStatus.OPEN)));
        repo.save(new Course("C2", "b", 1, CourseStatus.OPEN));
        assertEquals(1, snapshot.size());
        assertEquals(2, repo.findAll().size());
    }

    // ---- registrations (Set) ----

    @Test
    void registrationRepository_rejectsADuplicateStudentAndCourse() {
        InMemoryRegistrationRepository repo = new InMemoryRegistrationRepository();
        assertTrue(repo.add(reg("S1", "C1", 0)));
        assertFalse(repo.add(reg("S1", "C1", 99)));
        assertTrue(repo.add(reg("S1", "C2", 0)));
        assertEquals(2, repo.findAll().size());
    }

    @Test
    void registrationRepository_removesAndCountsByCourse() {
        InMemoryRegistrationRepository repo = new InMemoryRegistrationRepository();
        repo.add(reg("S1", "C1", 0));
        repo.add(reg("S2", "C1", 1));
        repo.add(reg("S1", "C2", 2));
        assertEquals(2, repo.countByCourse("c1"));
        assertTrue(repo.remove("S1", "C1").isPresent());
        assertTrue(repo.remove("S1", "C1").isEmpty());
        assertFalse(repo.exists("S1", "C1"));
        assertEquals(1, repo.countByCourse("C1"));
    }

    @Test
    void registrationRepository_ordersResults() {
        InMemoryRegistrationRepository repo = new InMemoryRegistrationRepository();
        repo.add(reg("S2", "C1", 20));
        repo.add(reg("S1", "C1", 10));
        repo.add(reg("S1", "B1", 30));
        assertEquals(List.of("S1", "S2"), repo.findByCourse("C1").stream().map(Registration::studentId).toList());
        assertEquals(List.of("B1", "C1"), repo.findByStudent("S1").stream().map(Registration::courseCode).toList());
    }

    @Test
    void registrationRepository_findAllCannotBeUsedToChangeTheRepository() {
        InMemoryRegistrationRepository repo = new InMemoryRegistrationRepository();
        repo.add(reg("S1", "C1", 0));
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().add(reg("S2", "C1", 1)));
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().clear());
        assertEquals(1, repo.findAll().size());
    }

    // ---- waiting lists (Deque) ----

    @Test
    void waitlist_isFirstInFirstOut() {
        InMemoryWaitlistRepository repo = new InMemoryWaitlistRepository();
        repo.enqueue(wait("S1", "C1"));
        repo.enqueue(wait("S2", "C1"));
        repo.enqueue(wait("S3", "C1"));
        assertEquals(List.of("S1", "S2", "S3"), repo.entries("C1").stream().map(WaitlistEntry::studentId).toList());
        assertEquals("S1", repo.pollFirstMatching("C1", e -> true).orElseThrow().studentId());
        assertEquals("S2", repo.pollFirstMatching("C1", e -> true).orElseThrow().studentId());
    }

    @Test
    void waitlist_rejectsDuplicates_andKeepsQueuesPerCourse() {
        InMemoryWaitlistRepository repo = new InMemoryWaitlistRepository();
        assertTrue(repo.enqueue(wait("S1", "C1")));
        assertFalse(repo.enqueue(wait("S1", "c1")));
        assertTrue(repo.enqueue(wait("S1", "C2")));
        assertEquals(1, repo.size("C1"));
        assertEquals(1, repo.size("C2"));
        assertEquals(0, repo.size("NOPE"));
    }

    @Test
    void pollFirstMatching_skipsIneligibleEntriesWithoutChangingTheirOrder() {
        InMemoryWaitlistRepository repo = new InMemoryWaitlistRepository();
        repo.enqueue(wait("S1", "C1"));
        repo.enqueue(wait("S2", "C1"));
        repo.enqueue(wait("S3", "C1"));
        WaitlistEntry chosen = repo.pollFirstMatching("C1", e -> !e.studentId().equals("S1")).orElseThrow();
        assertEquals("S2", chosen.studentId());
        assertEquals(List.of("S1", "S3"), repo.entries("C1").stream().map(WaitlistEntry::studentId).toList());
        assertTrue(repo.pollFirstMatching("C1", e -> false).isEmpty());
    }

    @Test
    void waitlist_removeTakesAStudentOutOfTheMiddle() {
        InMemoryWaitlistRepository repo = new InMemoryWaitlistRepository();
        repo.enqueue(wait("S1", "C1"));
        repo.enqueue(wait("S2", "C1"));
        repo.enqueue(wait("S3", "C1"));
        assertTrue(repo.remove("S2", "C1").isPresent());
        assertTrue(repo.remove("S2", "C1").isEmpty());
        assertEquals(List.of("S1", "S3"), repo.entries("C1").stream().map(WaitlistEntry::studentId).toList());
    }

    @Test
    void waitlistResults_areUnmodifiableCopies() {
        InMemoryWaitlistRepository repo = new InMemoryWaitlistRepository();
        repo.enqueue(wait("S1", "C1"));
        List<WaitlistEntry> snapshot = repo.entries("C1");
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().clear());
        assertEquals(1, repo.size("C1"));
    }
}
