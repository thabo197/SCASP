package edu.scasp.core.service;

import edu.scasp.core.model.Registration;
import edu.scasp.core.model.WaitlistEntry;
import java.util.List;

public interface RegistrationService {

    /**
     * Registers the student if a seat is free, otherwise adds them to the back of the waiting list.
     * Rejects duplicates, closed courses and inactive students.
     */
    RegistrationOutcome register(String actorId, String studentId, String courseCode);

    /** Withdraws a registration (and promotes the next eligible waiting student) or leaves the waiting list. */
    WithdrawalResult withdraw(String actorId, String studentId, String courseCode);

    boolean isRegistered(String studentId, String courseCode);

    List<Registration> registrationsForCourse(String courseCode);

    List<Registration> registrationsForStudent(String studentId);

    /** The waiting list in order, front first. */
    List<WaitlistEntry> waitlistFor(String courseCode);

    int availableSeats(String courseCode);
}
