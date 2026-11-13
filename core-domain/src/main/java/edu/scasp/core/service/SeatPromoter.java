package edu.scasp.core.service;

import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Registration;
import edu.scasp.core.model.RegistrationStatus;
import edu.scasp.core.model.User;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.repository.Repositories;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Moves waiting students into free seats. This rule is written once and used whenever a seat may have become
 * free (withdrawal, capacity increase, course reopened). The first waiting student who is eligible gets the seat;
 * ineligible students (for example suspended accounts) keep their place in the queue.
 */
final class SeatPromoter {

    private final Repositories repos;
    private final AuditTrail audit;
    private final Clock clock;

    SeatPromoter(Repositories repos, AuditTrail audit, Clock clock) {
        this.repos = repos;
        this.audit = audit;
        this.clock = clock;
    }

    List<Registration> promoteWhileSeatsFree(Course course) {
        List<Registration> promoted = new ArrayList<>();
        if (course.status() != CourseStatus.OPEN) {
            return promoted;
        }
        while (repos.registrations().countByCourse(course.courseCode()) < course.capacity()) {
            Optional<WaitlistEntry> next =
                    repos.waitlist().pollFirstMatching(course.courseCode(), this::isEligible);
            if (next.isEmpty()) {
                break;
            }
            Registration registration = new Registration(next.get().studentId(), course.courseCode(),
                    RegistrationStatus.ACTIVE, clock.instant());
            repos.registrations().add(registration);
            audit.record(AuditTrail.SYSTEM, "PROMOTE_FROM_WAITLIST", "Registration", key(registration));
            promoted.add(registration);
        }
        return promoted;
    }

    boolean isEligible(WaitlistEntry entry) {
        return repos.students().findById(entry.studentId())
                .flatMap(student -> repos.users().findById(student.userId()))
                .map(User::isActive)
                .orElse(false);
    }

    static String key(Registration registration) {
        return registration.studentId() + ":" + registration.courseCode();
    }
}
