package edu.scasp.core.report;

import java.util.List;

public record StudentRegistrationRow(String studentId, String name, List<String> registeredCourses,
                                     int waitlistedCount) {

    public StudentRegistrationRow {
        registeredCourses = List.copyOf(registeredCourses);
    }
}
