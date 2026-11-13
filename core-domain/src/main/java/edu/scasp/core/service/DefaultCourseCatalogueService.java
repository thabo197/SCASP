package edu.scasp.core.service;

import edu.scasp.core.exception.DuplicateEntityException;
import edu.scasp.core.exception.EntityNotFoundException;
import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.repository.Repositories;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class DefaultCourseCatalogueService implements CourseCatalogueService {

    private final Repositories repos;
    private final AccessPolicy access;
    private final AuditTrail audit;
    private final SeatPromoter promoter;

    DefaultCourseCatalogueService(Repositories repos, AccessPolicy access, AuditTrail audit,
                                  SeatPromoter promoter) {
        this.repos = repos;
        this.access = access;
        this.audit = audit;
        this.promoter = promoter;
    }

    @Override
    public Course createCourse(String actorId, String courseCode, String title, int capacity) {
        access.requireStaff(actorId);
        Course course = new Course(courseCode, title, capacity, CourseStatus.OPEN);
        if (repos.courses().existsById(course.courseCode())) {
            throw new DuplicateEntityException("Course already exists: " + course.courseCode());
        }
        repos.courses().save(course);
        audit.record(actorId, "CREATE_COURSE", "Course", course.courseCode());
        return course;
    }

    @Override
    public Course updateCourse(String actorId, String courseCode, String title, int capacity) {
        access.requireStaff(actorId);
        Course existing = require(courseCode);
        int registered = repos.registrations().countByCourse(existing.courseCode());
        Course updated = existing.withTitle(title).withCapacity(capacity);
        if (capacity < registered) {
            throw new InvalidOperationException("Capacity " + capacity + " is below the " + registered
                    + " students already registered for " + existing.courseCode());
        }
        repos.courses().save(updated);
        audit.record(actorId, "UPDATE_COURSE", "Course", updated.courseCode());
        promoter.promoteWhileSeatsFree(updated);
        return updated;
    }

    @Override
    public Course changeStatus(String actorId, String courseCode, CourseStatus newStatus) {
        access.requireStaff(actorId);
        Course existing = require(courseCode);
        if (!existing.status().canTransitionTo(newStatus)) {
            throw new InvalidOperationException("Course " + existing.courseCode() + " cannot change from "
                    + existing.status() + " to " + newStatus);
        }
        Course updated = existing.withStatus(newStatus);
        repos.courses().save(updated);
        audit.record(actorId, "CHANGE_COURSE_STATUS", "Course", updated.courseCode());
        promoter.promoteWhileSeatsFree(updated);
        return updated;
    }

    @Override
    public Optional<Course> findCourse(String courseCode) {
        return repos.courses().findById(Course.normalizeCode(courseCode));
    }

    @Override
    public List<Course> listCourses() {
        return repos.courses().findAll();
    }

    @Override
    public List<Course> listCourses(Comparator<? super Course> order) {
        return repos.courses().findAll().stream().sorted(order).toList();
    }

    @Override
    public List<Course> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listCourses();
        }
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        return repos.courses().findAll().stream()
                .filter(c -> c.courseCode().toLowerCase(Locale.ROOT).contains(needle)
                        || c.title().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    @Override
    public List<Course> findByStatus(CourseStatus status) {
        return repos.courses().findAll().stream().filter(c -> c.status() == status).toList();
    }

    private Course require(String courseCode) {
        String code = Course.normalizeCode(courseCode);
        return repos.courses().findById(code)
                .orElseThrow(() -> new EntityNotFoundException("Unknown course: " + code));
    }
}
