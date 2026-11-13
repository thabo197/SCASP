package edu.scasp.core.repository.memory;

import edu.scasp.core.model.AuditEvent;
import edu.scasp.core.repository.AuditEventRepository;
import java.util.ArrayList;
import java.util.List;

/** An ArrayList keeps events in the order they happened. */
public class InMemoryAuditEventRepository implements AuditEventRepository {

    private final List<AuditEvent> events = new ArrayList<>();

    @Override
    public void append(AuditEvent event) {
        events.add(event);
    }

    @Override
    public List<AuditEvent> findAll() {
        return List.copyOf(events);
    }
}
