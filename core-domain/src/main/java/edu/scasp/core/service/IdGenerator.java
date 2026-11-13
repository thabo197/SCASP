package edu.scasp.core.service;

/** Produces stable, unique identifiers such as REQ-0001. */
public interface IdGenerator {

    String next(String prefix);
}
