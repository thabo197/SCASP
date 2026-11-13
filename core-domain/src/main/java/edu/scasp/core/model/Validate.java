package edu.scasp.core.model;

import edu.scasp.core.exception.ValidationException;

/** Small argument checks shared by the domain objects. */
public final class Validate {

    private Validate() {
    }

    public static String notBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " must not be blank");
        }
        return value.trim();
    }

    public static <T> T notNull(String field, T value) {
        if (value == null) {
            throw new ValidationException(field + " must not be null");
        }
        return value;
    }

    public static int positive(String field, int value) {
        if (value <= 0) {
            throw new ValidationException(field + " must be greater than zero");
        }
        return value;
    }

    public static double inRange(String field, double value, double min, double max) {
        if (Double.isNaN(value) || value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max);
        }
        return value;
    }
}
