package edu.scasp.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** One counter per prefix. Thread-safe, so it is already safe to share with the Stage 2 workers. */
public class SequentialIdGenerator implements IdGenerator {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public String next(String prefix) {
        long n = counters.computeIfAbsent(prefix, p -> new AtomicLong()).incrementAndGet();
        return String.format("%s-%04d", prefix, n);
    }
}
