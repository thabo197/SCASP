package edu.scasp.core.exception;

/** A required student, course, request or other entity does not exist. */
public class EntityNotFoundException extends DomainException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
