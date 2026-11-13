package edu.scasp.core.exception;

/** A valid request that is not allowed in the current state (closed course, illegal status change). */
public class InvalidOperationException extends DomainException {

    private static final long serialVersionUID = 1L;

    public InvalidOperationException(String message) {
        super(message);
    }
}
