package edu.scasp.core.model;

import java.time.Instant;
import java.util.Objects;

/** A record of one important state change: who did what to which entity, and when. */
public record AuditEvent(String eventId, String actor, String action, String entityType, String entityId,
                         Instant occurredAt) {

    public AuditEvent {
        eventId = Validate.notBlank("eventId", eventId);
        actor = Validate.notBlank("actor", actor);
        action = Validate.notBlank("action", action);
        entityType = Validate.notBlank("entityType", entityType);
        entityId = Validate.notBlank("entityId", entityId);
        Validate.notNull("occurredAt", occurredAt);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AuditEvent other && eventId.equals(other.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
