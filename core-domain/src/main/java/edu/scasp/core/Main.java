package edu.scasp.core;

/** Baseline entry point: proves the project builds and runs on each member's machine. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println(ScaspInfo.banner());
        System.out.println("Java " + System.getProperty("java.version"));
    }
}
