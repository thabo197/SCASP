package edu.scasp.core.model;

/** Service request lifecycle: SUBMITTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, with REJECTED as an early exit. */
public enum RequestStatus {
    SUBMITTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, REJECTED;

    public boolean canTransitionTo(RequestStatus next) {
        return switch (this) {
            case SUBMITTED -> next == ASSIGNED || next == REJECTED;
            case ASSIGNED -> next == IN_PROGRESS || next == REJECTED;
            case IN_PROGRESS -> next == RESOLVED;
            case RESOLVED -> next == CLOSED;
            case CLOSED, REJECTED -> false;
        };
    }

    /** True while staff are still working on the request. */
    public boolean isBeingWorked() {
        return this == ASSIGNED || this == IN_PROGRESS;
    }
}
