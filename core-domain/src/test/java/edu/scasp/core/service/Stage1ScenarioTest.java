package edu.scasp.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.demo.SampleData;
import edu.scasp.core.model.CourseStatus;
import edu.scasp.core.model.Registration;
import edu.scasp.core.model.UserStatus;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.repository.Repositories;
import edu.scasp.core.repository.memory.InMemoryRepositories;
import edu.scasp.core.support.TestClock;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The Stage 1 minimum demonstration, as an automated test. */
class Stage1ScenarioTest {

    @Test
    void sampleData_loadsFictionalUsersStudentsCoursesAndCampusServices() {
        Repositories repos = InMemoryRepositories.create();
        SampleData.load(repos);
        assertEquals(8, repos.users().count());
        assertEquals(6, repos.students().count());
        assertEquals(5, repos.courses().count());
        assertEquals(3, repos.campusServices().count());
        assertEquals(CourseStatus.CLOSED, repos.courses().findById("BCS999").orElseThrow().status());
    }

    @Test
    void loadSampleData_registerStudents_fillACourse_waitlistTheRest_withdraw_promoteTheNextEligible() {
        Repositories repos = InMemoryRepositories.create();
        SampleData.load(repos);
        Platform platform = Platform.create(repos, new TestClock());
        RegistrationService registrations = platform.registrations();

        for (String s : List.of("S001", "S002", "S003", "S004", "S005", "S006")) {
            registrations.register("U" + s.substring(1), s, "BCS101");
        }
        assertEquals(List.of("S001", "S002", "S003"), ids(registrations.registrationsForCourse("BCS101")));
        assertEquals(List.of("S004", "S005", "S006"), waiting(registrations));

        // One student withdraws: the first waiting student takes the seat.
        registrations.withdraw("U002", "S002", "BCS101");
        assertEquals(List.of("S001", "S003", "S004"), ids(registrations.registrationsForCourse("BCS101")));
        assertEquals(List.of("S005", "S006"), waiting(registrations));

        // The next waiting student is suspended, so the following eligible student is promoted instead.
        repos.users().save(repos.users().findById("U005").orElseThrow().withStatus(UserStatus.SUSPENDED));
        registrations.withdraw("U001", "S001", "BCS101");
        assertEquals(List.of("S003", "S004", "S006"), ids(registrations.registrationsForCourse("BCS101")));
        assertEquals(List.of("S005"), waiting(registrations));
        assertTrue(registrations.availableSeats("BCS101") == 0);
    }

    private static List<String> ids(List<Registration> list) {
        return list.stream().map(Registration::studentId).sorted().toList();
    }

    private static List<String> waiting(RegistrationService registrations) {
        return registrations.waitlistFor("BCS101").stream().map(WaitlistEntry::studentId).toList();
    }
}
