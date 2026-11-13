package edu.scasp.core.model;

/** Course lifecycle: OPEN and CLOSED can switch back and forth; CANCELLED is final. */
public enum CourseStatus {
    OPEN, CLOSED, CANCELLED;

    public boolean canTransitionTo(CourseStatus next) {
        return switch (this) {
            case OPEN -> next == CLOSED || next == CANCELLED;
            case CLOSED -> next == OPEN || next == CANCELLED;
            case CANCELLED -> false;
        };
    }
}
