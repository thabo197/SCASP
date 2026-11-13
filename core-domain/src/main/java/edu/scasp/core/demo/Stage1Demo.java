package edu.scasp.core.demo;

import edu.scasp.core.exception.DomainException;
import edu.scasp.core.model.Audience;
import edu.scasp.core.model.Course;
import edu.scasp.core.model.CourseComparators;
import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestPriority;
import edu.scasp.core.model.ServiceRequest;
import edu.scasp.core.model.UserStatus;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.repository.Repositories;
import edu.scasp.core.repository.memory.InMemoryRepositories;
import edu.scasp.core.service.Platform;
import edu.scasp.core.service.RegistrationOutcome;
import edu.scasp.core.service.WithdrawalResult;
import java.time.Clock;

/**
 * Stage 1 demonstration: load sample data, fill a course, waiting-list extra students, withdraw one student and
 * promote the next eligible student. Run with:
 * java -cp core-domain/target/classes edu.scasp.core.demo.Stage1Demo
 */
public final class Stage1Demo {

    private static final String STAFF = SampleData.STAFF_ID;

    private Stage1Demo() {
    }

    public static void main(String[] args) {
        Repositories repos = InMemoryRepositories.create();
        SampleData.load(repos);
        Platform platform = Platform.create(repos, Clock.systemUTC());

        heading("1. Sample data loaded");
        System.out.println(repos.users().count() + " users, " + repos.students().count() + " students, "
                + repos.courses().count() + " courses, " + repos.campusServices().count() + " campus services");
        System.out.println("Catalogue sorted by title:");
        platform.courses().listCourses(CourseComparators.BY_TITLE).forEach(Stage1Demo::printCourse);

        heading("2. Fill BCS101 (capacity 3), then waiting-list the rest");
        for (String student : new String[] {"S001", "S002", "S003", "S004", "S005", "S006"}) {
            RegistrationOutcome outcome = platform.registrations().register(userOf(student), student, "BCS101");
            System.out.println(student + " -> " + outcome.status()
                    + (outcome.isRegistered() ? "" : " at position " + outcome.waitlistPosition()));
        }
        printWaitlist(platform, "BCS101");

        heading("3. Duplicate and invalid operations are rejected");
        attempt("S001 registers for BCS101 again",
                () -> platform.registrations().register(userOf("S001"), "S001", "BCS101"));
        attempt("S001 registers for the closed course BCS999",
                () -> platform.registrations().register(userOf("S001"), "S001", "BCS999"));
        attempt("S002 tries to register S003 (a student acting for someone else)",
                () -> platform.registrations().register(userOf("S002"), "S003", "BCS102"));

        heading("4. S002 withdraws: the first waiting student is promoted");
        WithdrawalResult first = platform.registrations().withdraw(userOf("S002"), "S002", "BCS101");
        System.out.println("S002 withdrew; promoted: " + first.promoted().stream().map(r -> r.studentId()).toList());
        printWaitlist(platform, "BCS101");

        heading("5. S005 is suspended, so the next withdrawal skips them and promotes S006");
        repos.users().save(repos.users().findById("U005").orElseThrow().withStatus(UserStatus.SUSPENDED));
        WithdrawalResult second = platform.registrations().withdraw(userOf("S001"), "S001", "BCS101");
        System.out.println("S001 withdrew; promoted: " + second.promoted().stream().map(r -> r.studentId()).toList());
        printWaitlist(platform, "BCS101");

        heading("6. Service requests come out of the priority queue most urgent first");
        platform.serviceDesk().submit(userOf("S003"), "S003", RequestCategory.TRANSCRIPT, RequestPriority.NORMAL);
        platform.serviceDesk().submit(userOf("S004"), "S004", RequestCategory.APPEAL_ENQUIRY, RequestPriority.URGENT);
        platform.serviceDesk().submit(userOf("S006"), "S006", RequestCategory.CLEARANCE, RequestPriority.HIGH);
        platform.serviceDesk().submit(userOf("S003"), "S003", RequestCategory.OTHER, RequestPriority.URGENT);
        System.out.println("Queue: " + describe(platform.serviceDesk().pendingQueue()));
        for (int i = 0; i < 2; i++) {
            platform.serviceDesk().assignNext(STAFF, STAFF)
                    .ifPresent(r -> System.out.println("Assigned next: " + r.requestId() + " " + r.priority()
                            + " " + r.category() + " to " + r.assignedTo()));
        }
        platform.announcements().publish(STAFF, "Registration is open", "Course registration closes Friday.",
                Audience.STUDENTS);

        heading("7. Reports");
        System.out.println("Capacity:");
        platform.reports().capacityReport().forEach(row -> System.out.printf(
                "  %-7s %-30s %-9s capacity %d, registered %d, available %d, waiting %d%n",
                row.courseCode(), row.title(), row.status(), row.capacity(), row.registered(), row.available(),
                row.waitlisted()));
        System.out.println("Waiting lists: " + platform.reports().waitingListReport());
        System.out.println("Request workload: " + platform.reports().requestWorkloadReport());
        System.out.println("Registrations per course: " + platform.reports().registrationsPerCourseStatistics());
        System.out.println("Audit events recorded: " + platform.auditTrail().events().size());
        platform.auditTrail().events().stream().skip(Math.max(0, platform.auditTrail().events().size() - 5))
                .forEach(e -> System.out.println("  " + e.actor() + " " + e.action() + " " + e.entityType()
                        + " " + e.entityId()));
    }

    private static String userOf(String studentId) {
        return "U" + studentId.substring(1);
    }

    private static void heading(String text) {
        System.out.println();
        System.out.println("== " + text + " ==");
    }

    private static void printCourse(Course c) {
        System.out.println("  " + c.courseCode() + "  " + c.title() + "  capacity " + c.capacity() + "  " + c.status());
    }

    private static void printWaitlist(Platform platform, String code) {
        System.out.println("Waiting list " + code + ": "
                + platform.registrations().waitlistFor(code).stream().map(WaitlistEntry::studentId).toList()
                + "; registered: "
                + platform.registrations().registrationsForCourse(code).stream().map(r -> r.studentId()).toList());
    }

    private static String describe(java.util.List<ServiceRequest> requests) {
        return requests.stream().map(r -> r.requestId() + "(" + r.priority() + ")").toList().toString();
    }

    private static void attempt(String description, Runnable action) {
        try {
            action.run();
            System.out.println(description + " -> allowed (unexpected)");
        } catch (DomainException e) {
            System.out.println(description + " -> rejected: " + e.getMessage());
        }
    }
}
