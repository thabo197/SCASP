package edu.scasp.core.report;

import java.util.List;

/** The students waiting for one course, front of the queue first. */
public record WaitlistRow(String courseCode, String title, List<String> studentIds) {

    public WaitlistRow {
        studentIds = List.copyOf(studentIds);
    }
}
