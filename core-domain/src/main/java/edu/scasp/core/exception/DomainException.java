package edu.scasp.core.exception;

/** Base class for all rule violations raised by the domain layer. Unchecked so callers can choose where to handle it. */
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super(message);
    }
}
