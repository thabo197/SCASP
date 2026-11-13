package edu.scasp.core.model;

import java.util.Objects;

/** A platform account. Identity (equals/hashCode) is the userId. */
public record User(String userId, String name, Role role, UserStatus status) {

    public User {
        userId = Validate.notBlank("userId", userId);
        name = Validate.notBlank("name", name);
        Validate.notNull("role", role);
        Validate.notNull("status", status);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public User withStatus(UserStatus newStatus) {
        return new User(userId, name, role, newStatus);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User other && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
