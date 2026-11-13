package edu.scasp.core.model;

public enum Audience {
    ALL, STUDENTS, STAFF;

    /** Does an announcement for this audience reach a user with the given role? */
    public boolean includes(Role role) {
        return switch (this) {
            case ALL -> true;
            case STUDENTS -> role == Role.STUDENT;
            case STAFF -> role.isStaffOrAdmin();
        };
    }
}
