package edu.scasp.core.exception;

/** An entity that must be unique already exists (duplicate registration, duplicate course). */
public class DuplicateEntityException extends DomainException {

    private static final long serialVersionUID = 1L;

    public DuplicateEntityException(String message) {
        super(message);
    }
}
