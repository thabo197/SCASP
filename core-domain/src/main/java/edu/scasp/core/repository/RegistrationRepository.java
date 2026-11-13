package edu.scasp.core.repository;

import edu.scasp.core.model.Registration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Holds active registrations in a Set, so a student can never appear twice for the same course. */
public interface RegistrationRepository {

    /** Returns false, and changes nothing, if the student is already registered for the course. */
    boolean add(Registration registration);

    Optional<Registration> remove(String studentId, String courseCode);

    boolean exists(String studentId, String courseCode);

    int countByCourse(String courseCode);

    /** Registrations for a course, oldest first. */
    List<Registration> findByCourse(String courseCode);

    /** Registrations for a student, ordered by course code. */
    List<Registration> findByStudent(String studentId);

    /** An unmodifiable snapshot of every active registration. */
    Set<Registration> findAll();
}
