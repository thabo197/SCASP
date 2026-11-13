package edu.scasp.core.service;

import edu.scasp.core.model.Announcement;
import edu.scasp.core.model.Audience;
import edu.scasp.core.model.Role;
import java.util.List;

public interface AnnouncementService {

    /** Staff only. */
    Announcement publish(String actorId, String title, String body, Audience audience);

    /** Announcements that reach the given role, newest first. */
    List<Announcement> announcementsFor(Role role);
}
