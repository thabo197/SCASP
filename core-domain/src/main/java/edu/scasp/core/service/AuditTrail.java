package edu.scasp.core.service;

import edu.scasp.core.model.AuditEvent;
import edu.scasp.core.repository.AuditEventRepository;
import java.time.Clock;
import java.util.List;

/** Records who did what, and when (invariant I-09). Events come back as unmodifiable lists. */
public final class AuditTrail {

    public static final String SYSTEM = "SYSTEM";

    private final AuditEventRepository repository;
    private final IdGenerator ids;
    private final Clock clock;

    public AuditTrail(AuditEventRepository repository, IdGenerator ids, Clock clock) {
        this.repository = repository;
        this.ids = ids;
        this.clock = clock;
    }

    public AuditEvent record(String actor, String action, String entityType, String entityId) {
        AuditEvent event = new AuditEvent(ids.next("AUD"), actor, action, entityType, entityId, clock.instant());
        repository.append(event);
        return event;
    }

    public List<AuditEvent> events() {
        return repository.findAll();
    }

    public List<AuditEvent> eventsFor(String entityType, String entityId) {
        return repository.findAll().stream()
                .filter(e -> e.entityType().equals(entityType) && e.entityId().equals(entityId))
                .toList();
    }
}
