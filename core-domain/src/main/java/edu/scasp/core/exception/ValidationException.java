package edu.scasp.core.exception;

/** Input or object state that breaks a basic rule (blank, out of range, null). */
public class ValidationException extends DomainException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
