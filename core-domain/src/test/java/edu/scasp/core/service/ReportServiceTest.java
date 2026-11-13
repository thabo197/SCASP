package edu.scasp.core.service;

import static edu.scasp.core.support.Fixture.ADMIN;
import static edu.scasp.core.support.Fixture.STAFF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.model.RequestCategory;
import edu.scasp.core.model.RequestPriority;
import edu.scasp.core.model.RequestStatus;
import edu.scasp.core.report.CourseCapacityRow;
import edu.scasp.core.report.RequestWorkloadReport;
import edu.scasp.core.report.StudentRegistrationRow;
import edu.scasp.core.report.WaitlistRow;
import edu.scasp.core.support.Fixture;
import java.util.IntSummaryStatistics;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportServiceTest {

    private static Fixture busyFixture() {
        Fixture f = new Fixture(5).course("C1", 2).course("C2", 3).course("C3", 1);
        f.register("S1", "C1");
        f.register("S2", "C1");
        f.register("S3", "C1");   // waits
        f.register("S4", "C1");   // waits
        f.register("S1", "C2");
        f.register("S5", "C3");
        f.register("S1", "C3");   // waits
        return f;
    }

    @Test
    void capacityReport_showsRegisteredAvailableAndWaiting_inCourseOrder() {
        List<CourseCapacityRow> rows = busyFixture().platform.reports().capacityReport();
        assertEquals(List.of("C1", "C2", "C3"), rows.stream().map(CourseCapacityRow::courseCode).toList());
        CourseCapacityRow c1 = rows.get(0);
        assertEquals(2, c1.capacity());
        assertEquals(2, c1.registered());
        assertEquals(0, c1.available());
        assertEquals(2, c1.waitlisted());
        CourseCapacityRow c2 = rows.get(1);
        assertEquals(1, c2.registered());
        assertEquals(2, c2.available());
        assertEquals(0, c2.waitlisted());
    }

    @Test
    void waitingListReport_listsOnlyCoursesWithWaitingStudents_longestQueueFirst() {
        List<WaitlistRow> rows = busyFixture().platform.reports().waitingListReport();
        assertEquals(List.of("C1", "C3"), rows.stream().map(WaitlistRow::courseCode).toList());
        assertEquals(List.of("S3", "S4"), rows.get(0).studentIds());
        assertEquals(List.of("S1"), rows.get(1).studentIds());
    }

    @Test
    void studentRegistrationReport_listsEveryStudentInIdOrder() {
        List<StudentRegistrationRow> rows = busyFixture().platform.reports().studentRegistrationReport();
        assertEquals(List.of("S1", "S2", "S3", "S4", "S5"),
                rows.stream().map(StudentRegistrationRow::studentId).toList());
        StudentRegistrationRow s1 = rows.get(0);
        assertEquals("Student 1", s1.name());
        assertEquals(List.of("C1", "C2"), s1.registeredCourses());
        assertEquals(1, s1.waitlistedCount());
        assertTrue(rows.get(2).registeredCourses().isEmpty());
        assertEquals(1, rows.get(2).waitlistedCount());
    }

    @Test
    void registrationStatistics_summariseRegistrationsPerCourse() {
        IntSummaryStatistics stats = busyFixture().platform.reports().registrationsPerCourseStatistics();
        assertEquals(3, stats.getCount());
        assertEquals(4, stats.getSum());
        assertEquals(1, stats.getMin());
        assertEquals(2, stats.getMax());
    }

    @Test
    void workloadReport_countsRequestsByStatusCategoryAndAssignee_includingZeros() {
        Fixture f = new Fixture(3);
        var desk = f.platform.serviceDesk();
        var r1 = desk.submit("U1", "S1", RequestCategory.TRANSCRIPT, RequestPriority.URGENT);
        var r2 = desk.submit("U2", "S2", RequestCategory.CLEARANCE, RequestPriority.NORMAL);
        desk.submit("U3", "S3", RequestCategory.TRANSCRIPT, RequestPriority.LOW);
        desk.assign(STAFF, r1.requestId(), STAFF);
        desk.assign(STAFF, r2.requestId(), ADMIN);
        desk.changeStatus(STAFF, r2.requestId(), RequestStatus.IN_PROGRESS);

        RequestWorkloadReport report = f.platform.reports().requestWorkloadReport();
        assertEquals(1L, report.byStatus().get(RequestStatus.SUBMITTED));
        assertEquals(1L, report.byStatus().get(RequestStatus.ASSIGNED));
        assertEquals(1L, report.byStatus().get(RequestStatus.IN_PROGRESS));
        assertEquals(0L, report.byStatus().get(RequestStatus.CLOSED));
        assertEquals(2L, report.byCategory().get(RequestCategory.TRANSCRIPT));
        assertEquals(0L, report.byCategory().get(RequestCategory.OTHER));
        assertEquals(1L, report.openByAssignee().get(STAFF));
        assertEquals(1L, report.openByAssignee().get(ADMIN));
        assertEquals(1L, report.unassigned());
    }

    @Test
    void emptyPlatform_producesEmptyReports() {
        Fixture f = new Fixture(0);
        assertTrue(f.platform.reports().capacityReport().isEmpty());
        assertTrue(f.platform.reports().waitingListReport().isEmpty());
        assertEquals(0L, f.platform.reports().requestWorkloadReport().unassigned());
        assertEquals(0, f.platform.reports().registrationsPerCourseStatistics().getCount());
    }

    @Test
    void reportObjects_cannotBeModified() {
        Fixture f = busyFixture();
        assertThrows(UnsupportedOperationException.class, () -> f.platform.reports().capacityReport().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> f.platform.reports().requestWorkloadReport().byStatus().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> f.platform.reports().waitingListReport().get(0).studentIds().clear());
    }
}
