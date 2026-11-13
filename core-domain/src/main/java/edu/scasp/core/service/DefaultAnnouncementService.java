package edu.scasp.core.service;

import edu.scasp.core.model.Announcement;
import edu.scasp.core.model.Audience;
import edu.scasp.core.model.Role;
import edu.scasp.core.repository.Repositories;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;

public final class DefaultAnnouncementService implements AnnouncementService {

    private final Repositories repos;
    private final AccessPolicy access;
    private final AuditTrail audit;
    private final Clock clock;
    private final IdGenerator ids;

    DefaultAnnouncementService(Repositories repos, AccessPolicy access, AuditTrail audit, Clock clock,
                               IdGenerator ids) {
        this.repos = repos;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
        this.ids = ids;
    }

    @Override
    public Announcement publish(String actorId, String title, String body, Audience audience) {
        access.requireStaff(actorId);
        Announcement announcement = new Announcement(ids.next("ANN"), title, body, audience, clock.instant());
        repos.announcements().save(announcement);
        audit.record(actorId, "PUBLISH_ANNOUNCEMENT", "Announcement", announcement.announcementId());
        return announcement;
    }

    @Override
    public List<Announcement> announcementsFor(Role role) {
        return repos.announcements().findAll().stream()
                .filter(a -> a.audience().includes(role))
                .sorted(Comparator.comparing(Announcement::publishedAt).thenComparing(Announcement::announcementId)
                        .reversed())
                .toList();
    }
}
