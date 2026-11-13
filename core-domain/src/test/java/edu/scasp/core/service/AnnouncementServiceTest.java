package edu.scasp.core.service;

import static edu.scasp.core.support.Fixture.STAFF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.scasp.core.exception.UnauthorizedOperationException;
import edu.scasp.core.exception.ValidationException;
import edu.scasp.core.model.Announcement;
import edu.scasp.core.model.Audience;
import edu.scasp.core.model.Role;
import edu.scasp.core.support.Fixture;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnnouncementServiceTest {

    private static List<String> titles(List<Announcement> list) {
        return list.stream().map(Announcement::title).toList();
    }

    @Test
    void staffCanPublish_studentsCannot() {
        Fixture f = new Fixture(1);
        Announcement a = f.platform.announcements().publish(STAFF, "Welcome", "Term starts", Audience.ALL);
        assertEquals("ANN-0001", a.announcementId());
        assertThrows(UnauthorizedOperationException.class,
                () -> f.platform.announcements().publish("U1", "Hack", "x", Audience.ALL));
        assertEquals(1, f.platform.announcements().announcementsFor(Role.STUDENT).size());
    }

    @Test
    void blankTitleOrBody_isRejected() {
        Fixture f = new Fixture(0);
        assertThrows(ValidationException.class, () -> f.platform.announcements().publish(STAFF, " ", "body", Audience.ALL));
        assertThrows(ValidationException.class, () -> f.platform.announcements().publish(STAFF, "t", "", Audience.ALL));
    }

    @Test
    void retrieval_filtersByAudience_newestFirst() {
        Fixture f = new Fixture(0);
        f.platform.announcements().publish(STAFF, "For everyone", "b", Audience.ALL);
        f.platform.announcements().publish(STAFF, "For students", "b", Audience.STUDENTS);
        f.platform.announcements().publish(STAFF, "For staff", "b", Audience.STAFF);
        assertEquals(List.of("For students", "For everyone"), titles(f.platform.announcements().announcementsFor(Role.STUDENT)));
        assertEquals(List.of("For staff", "For everyone"), titles(f.platform.announcements().announcementsFor(Role.STAFF)));
        assertEquals(List.of("For staff", "For everyone"), titles(f.platform.announcements().announcementsFor(Role.ADMIN)));
    }

    @Test
    void results_areUnmodifiable() {
        Fixture f = new Fixture(0);
        f.platform.announcements().publish(STAFF, "t", "b", Audience.ALL);
        assertThrows(UnsupportedOperationException.class,
                () -> f.platform.announcements().announcementsFor(Role.STUDENT).clear());
        assertTrue(f.platform.announcements().announcementsFor(Role.STUDENT).size() == 1);
    }
}
