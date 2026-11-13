package edu.scasp.core.service;

import edu.scasp.core.report.CourseCapacityRow;
import edu.scasp.core.report.RequestWorkloadReport;
import edu.scasp.core.report.StudentRegistrationRow;
import edu.scasp.core.report.WaitlistRow;
import java.util.IntSummaryStatistics;
import java.util.List;

public interface ReportService {

    /** One row per course, in course-code order: capacity, registered, available seats, waiting. */
    List<CourseCapacityRow> capacityReport();

    /** Courses that have students waiting, longest queue first. */
    List<WaitlistRow> waitingListReport();

    RequestWorkloadReport requestWorkloadReport();

    /** One row per student, in student-id order. */
    List<StudentRegistrationRow> studentRegistrationReport();

    /** Count, minimum, maximum, sum and average of registrations per course. */
    IntSummaryStatistics registrationsPerCourseStatistics();
}
