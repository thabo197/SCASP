package edu.scasp.core.service;

import edu.scasp.core.model.Registration;
import java.util.List;

/**
 * The result of a withdrawal. {@code withdrawn} is null when the student only left the waiting list;
 * {@code promoted} lists students moved from the waiting list into the freed seat (at most one for a single seat).
 */
public record WithdrawalResult(Kind kind, Registration withdrawn, List<Registration> promoted) {

    public enum Kind { REGISTRATION_WITHDRAWN, REMOVED_FROM_WAITLIST }

    public WithdrawalResult {
        promoted = List.copyOf(promoted);
    }
}
