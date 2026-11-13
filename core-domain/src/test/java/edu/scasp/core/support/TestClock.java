package edu.scasp.core.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock that moves forward one second every time it is read, so every timestamp is distinct and predictable. */
public final class TestClock extends Clock {

    private Instant now = Instant.parse("2026-01-01T00:00:00Z");

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        Instant current = now;
        now = now.plusSeconds(1);
        return current;
    }
}
