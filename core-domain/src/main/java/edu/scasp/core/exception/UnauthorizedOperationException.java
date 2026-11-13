package edu.scasp.core.exception;

/** The acting user is unknown, inactive, or not allowed to perform the operation. */
public class UnauthorizedOperationException extends DomainException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
