package edu.scasp.core.service;

import edu.scasp.core.model.Registration;
import edu.scasp.core.model.WaitlistEntry;

/** What happened when a student asked for a seat: registered, or placed on the waiting list. */
public record RegistrationOutcome(Status status, Registration registration, WaitlistEntry waitlistEntry,
                                  int waitlistPosition) {

    public enum Status { REGISTERED, WAITLISTED }

    static RegistrationOutcome registered(Registration registration) {
        return new RegistrationOutcome(Status.REGISTERED, registration, null, 0);
    }

    static RegistrationOutcome waitlisted(WaitlistEntry entry, int position) {
        return new RegistrationOutcome(Status.WAITLISTED, null, entry, position);
    }

    public boolean isRegistered() {
        return status == Status.REGISTERED;
    }
}
