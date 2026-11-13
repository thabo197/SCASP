package edu.scasp.core.repository;

import edu.scasp.core.model.AuditEvent;
import java.util.List;

/** Append-only: audit events are never changed or deleted. */
public interface AuditEventRepository {

    void append(AuditEvent event);

    /** All events in the order they were recorded (a copy). */
    List<AuditEvent> findAll();
}
