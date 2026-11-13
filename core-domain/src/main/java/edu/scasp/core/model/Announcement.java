package edu.scasp.core.model;

import java.time.Instant;
import java.util.Objects;

public record Announcement(String announcementId, String title, String body, Audience audience,
                           Instant publishedAt) {

    public Announcement {
        announcementId = Validate.notBlank("announcementId", announcementId);
        title = Validate.notBlank("title", title);
        body = Validate.notBlank("body", body);
        Validate.notNull("audience", audience);
        Validate.notNull("publishedAt", publishedAt);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Announcement other && announcementId.equals(other.announcementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(announcementId);
    }
}
