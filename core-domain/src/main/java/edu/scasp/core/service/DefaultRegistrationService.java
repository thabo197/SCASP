package edu.scasp.core.service;

import edu.scasp.core.exception.DuplicateEntityException;
import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Registration;
import edu.scasp.core.model.RegistrationStatus;
import edu.scasp.core.model.Student;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.repository.Repositories;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Registration, waiting list and promotion rules. Stage 1 is deliberately single-threaded; Stage 2 makes these
 * operations safe for concurrent use.
 */
public final class DefaultRegistrationService implements RegistrationService {

    private final Repositories repos;
    private final AccessPolicy access;
    private final AuditTrail audit;
    private final Clock clock;
    private final SeatPromoter promoter;

    DefaultRegistrationService(Repositories repos, AccessPolicy access, AuditTrail audit, Clock clock,
                               SeatPromoter promoter) {
        this.repos = repos;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
        this.promoter = promoter;
    }

    @Override
    public RegistrationOutcome register(String actorId, String studentId, String courseCode) {
        access.requireSelfOrStaff(actorId, studentId);
        Student student = requireStudent(studentId);
        Course course = requireCourse(courseCode);
        String code = course.courseCode();

        if (!isActive(student)) {
            throw new InvalidOperationException("Student " + student.studentId() + " is not active");
        }
        if (course.status() != CourseStatus.OPEN) {
            throw new InvalidOperationException("Course " + code + " is not open for registration");
        }
        if (repos.registrations().exists(student.studentId(), code)) {
            throw new DuplicateEntityException(
                    "Student " + student.studentId() + " is already registered for " + code);
        }
        if (repos.waitlist().contains(student.studentId(), code)) {
            throw new DuplicateEntityException(
                    "Student " + student.studentId() + " is already on the waiting list for " + code);
        }

        if (repos.registrations().countByCourse(code) < course.capacity()) {
            Registration registration =
                    new Registration(student.studentId(), code, RegistrationStatus.ACTIVE, clock.instant());
            repos.registrations().add(registration);
            audit.record(actorId, "REGISTER", "Registration", SeatPromoter.key(registration));
            return RegistrationOutcome.registered(registration);
        }

        WaitlistEntry entry = new WaitlistEntry(student.studentId(), code, clock.instant(), 0);
        repos.waitlist().enqueue(entry);
        audit.record(actorId, "JOIN_WAITLIST", "WaitlistEntry", student.studentId() + ":" + code);
        return RegistrationOutcome.waitlisted(entry, repos.waitlist().size(code));
    }

    @Override
    public WithdrawalResult withdraw(String actorId, String studentId, String courseCode) {
        access.requireSelfOrStaff(actorId, studentId);
        Student student = requireStudent(studentId);
        Course course = requireCourse(courseCode);
        String code = course.courseCode();

        Optional<Registration> removed = repos.registrations().remove(student.studentId(), code);
        if (removed.isPresent()) {
            audit.record(actorId, "WITHDRAW", "Registration", SeatPromoter.key(removed.get()));
            List<Registration> promoted = promoter.promoteWhileSeatsFree(course);
            return new WithdrawalResult(WithdrawalResult.Kind.REGISTRATION_WITHDRAWN,
                    removed.get().withStatus(RegistrationStatus.WITHDRAWN), promoted);
        }

        Optional<WaitlistEntry> left = repos.waitlist().remove(student.studentId(), code);
        if (left.isPresent()) {
            audit.record(actorId, "LEAVE_WAITLIST", "WaitlistEntry", student.studentId() + ":" + code);
            return new WithdrawalResult(WithdrawalResult.Kind.REMOVED_FROM_WAITLIST, null, List.of());
        }
        throw new EntityNotFoundException(
                "Student " + student.studentId() + " is neither registered nor waiting for " + code);
    }

    @Override
    public boolean isRegistered(String studentId, String courseCode) {
        return repos.registrations().exists(studentId, courseCode);
    }

    @Override
    public List<Registration> registrationsForCourse(String courseCode) {
        return repos.registrations().findByCourse(requireCourse(courseCode).courseCode());
    }

    @Override
    public List<Registration> registrationsForStudent(String studentId) {
        return repos.registrations().findByStudent(requireStudent(studentId).studentId());
    }

    @Override
    public List<WaitlistEntry> waitlistFor(String courseCode) {
        return repos.waitlist().entries(requireCourse(courseCode).courseCode());
    }

    @Override
    public int availableSeats(String courseCode) {
        Course course = requireCourse(courseCode);
        return Math.max(0, course.capacity() - repos.registrations().countByCourse(course.courseCode()));
    }

    private boolean isActive(Student student) {
        return repos.users().findById(student.userId()).map(u -> u.isActive()).orElse(false);
    }

    private Student requireStudent(String studentId) {
        return repos.students().findById(studentId == null ? "" : studentId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Unknown student: " + studentId));
    }

    private Course requireCourse(String courseCode) {
        String code = Course.normalizeCode(courseCode);
        return repos.courses().findById(code)
                .orElseThrow(() -> new EntityNotFoundException("Unknown course: " + code));
    }
}
