package edu.scasp.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SequentialIdGeneratorTest {

    @Test
    void ids_areSequentialPerPrefix_andStable() {
        SequentialIdGenerator ids = new SequentialIdGenerator();
        assertEquals("REQ-0001", ids.next("REQ"));
        assertEquals("REQ-0002", ids.next("REQ"));
        assertEquals("ANN-0001", ids.next("ANN"));
        assertEquals("REQ-0003", ids.next("REQ"));
    }
}
