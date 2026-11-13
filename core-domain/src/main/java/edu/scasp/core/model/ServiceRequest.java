package edu.scasp.core.model;

import edu.scasp.core.exception.InvalidOperationException;
import edu.scasp.core.exception.ValidationException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * An academic service request. Immutable: every change returns a new object.
 * Natural order (used by the PriorityQueue): highest priority first, then oldest first, then requestId.
 * Identity is the requestId.
 */
public record ServiceRequest(String requestId, String studentId, RequestCategory category,
                             RequestPriority priority, RequestStatus status, String assignedTo,
                             Instant createdAt, Instant updatedAt) implements Comparable<ServiceRequest> {

    public ServiceRequest {
        requestId = Validate.notBlank("requestId", requestId);
        studentId = Validate.notBlank("studentId", studentId);
        Validate.notNull("category", category);
        Validate.notNull("priority", priority);
        Validate.notNull("status", status);
        Validate.notNull("createdAt", createdAt);
        Validate.notNull("updatedAt", updatedAt);
        assignedTo = (assignedTo == null || assignedTo.isBlank()) ? null : assignedTo.trim();
        if (updatedAt.isBefore(createdAt)) {
            throw new ValidationException("updatedAt must not be before createdAt");
        }
    }

    public static ServiceRequest submitted(String requestId, String studentId, RequestCategory category,
                                           RequestPriority priority, Instant now) {
        return new ServiceRequest(requestId, studentId, category, priority, RequestStatus.SUBMITTED, null, now, now);
    }

    public Optional<String> assignee() {
        return Optional.ofNullable(assignedTo);
    }

    /** SUBMITTED to ASSIGNED, recording who will handle it. */
    public ServiceRequest assignTo(String staffUserId, Instant now) {
        requireTransition(RequestStatus.ASSIGNED);
        return new ServiceRequest(requestId, studentId, category, priority, RequestStatus.ASSIGNED,
                Validate.notBlank("staffUserId", staffUserId), createdAt, now);
    }

    /** Any other allowed status change. Use assignTo for ASSIGNED, because that also needs an assignee. */
    public ServiceRequest withStatus(RequestStatus next, Instant now) {
        if (next == RequestStatus.ASSIGNED) {
            throw new InvalidOperationException("Use assignTo to assign request " + requestId);
        }
        requireTransition(next);
        return new ServiceRequest(requestId, studentId, category, priority, next, assignedTo, createdAt, now);
    }

    private void requireTransition(RequestStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidOperationException(
                    "Request " + requestId + " cannot change from " + status + " to " + next);
        }
    }

    @Override
    public int compareTo(ServiceRequest other) {
        int byPriority = other.priority.compareTo(priority);
        if (byPriority != 0) {
            return byPriority;
        }
        int byAge = createdAt.compareTo(other.createdAt);
        return byAge != 0 ? byAge : requestId.compareTo(other.requestId);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ServiceRequest other && requestId.equals(other.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }
}
