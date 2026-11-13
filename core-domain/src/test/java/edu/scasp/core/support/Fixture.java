package edu.scasp.core.support;

import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Role;
import edu.scasp.core.model.Student;
import edu.scasp.core.model.User;
import edu.scasp.core.model.UserStatus;
import edu.scasp.core.repository.Repositories;
import edu.scasp.core.repository.memory.InMemoryRepositories;
import edu.scasp.core.service.Platform;

/** A small, predictable platform for tests: students S1..Sn (users U1..Un), one staff user and one admin. */
public final class Fixture {

    public static final String STAFF = "U-STAFF";
    public static final String ADMIN = "U-ADMIN";

    public final Repositories repos = InMemoryRepositories.create();
    public final TestClock clock = new TestClock();
    public final Platform platform;

    public Fixture(int students) {
        repos.users().save(new User(STAFF, "Staff One", Role.STAFF, UserStatus.ACTIVE));
        repos.users().save(new User(ADMIN, "Admin One", Role.ADMIN, UserStatus.ACTIVE));
        for (int i = 1; i <= students; i++) {
            repos.users().save(new User("U" + i, "Student " + i, Role.STUDENT, UserStatus.ACTIVE));
            repos.students().save(new Student("S" + i, "U" + i, "BSc Test", 1));
        }
        platform = Platform.create(repos, clock);
    }

    public Fixture course(String code, int capacity) {
        repos.courses().save(new Course(code, "Title " + code, capacity, CourseStatus.OPEN));
        return this;
    }

    public static String userOf(String studentId) {
        return "U" + studentId.substring(1);
    }

    public void suspend(String userId) {
        repos.users().save(repos.users().findById(userId).orElseThrow().withStatus(UserStatus.SUSPENDED));
    }

    /** Registers the student, acting as that student. */
    public void register(String studentId, String courseCode) {
        platform.registrations().register(userOf(studentId), studentId, courseCode);
    }
}
