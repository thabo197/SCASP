package edu.scasp.core.model;

public enum Role {
    STUDENT, STAFF, ADMIN;

    public boolean isStaffOrAdmin() {
        return this == STAFF || this == ADMIN;
    }
}
