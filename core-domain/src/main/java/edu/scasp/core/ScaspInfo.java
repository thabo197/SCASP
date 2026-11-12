package edu.scasp.core;

/**
 * Placeholder so the Stage 0 baseline has something to compile and test.
 * The real domain classes replace this in Stage 1.
 */
public final class ScaspInfo {

    public static final String SHORT_NAME = "SCASP";
    public static final String NAME = "Smart Campus Academic Services Platform";

    private ScaspInfo() {
    }

    public static String banner() {
        return SHORT_NAME + " - " + NAME;
    }
}
