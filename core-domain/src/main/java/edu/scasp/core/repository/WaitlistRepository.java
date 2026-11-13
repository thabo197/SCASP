package edu.scasp.core.repository;

import edu.scasp.core.model.WaitlistEntry;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** One first-in, first-out queue per course. */
public interface WaitlistRepository {

    /** Adds to the back of the course's queue. Returns false if the student is already waiting for that course. */
    boolean enqueue(WaitlistEntry entry);

    boolean contains(String studentId, String courseCode);

    Optional<WaitlistEntry> remove(String studentId, String courseCode);

    /** The queue for a course in order, front first (a copy). */
    List<WaitlistEntry> entries(String courseCode);

    int size(String courseCode);

    /** Removes and returns the first entry (front first) that satisfies the test; entries before it stay put. */
    Optional<WaitlistEntry> pollFirstMatching(String courseCode, Predicate<WaitlistEntry> eligible);

    /** Every waiting entry across all courses (a copy). */
    List<WaitlistEntry> findAll();
}
