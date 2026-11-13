package edu.scasp.core.report;

import edu.scasp.core.model.CourseStatus;

public record CourseCapacityRow(String courseCode, String title, CourseStatus status, int capacity,
                                int registered, int available, int waitlisted) {
}
