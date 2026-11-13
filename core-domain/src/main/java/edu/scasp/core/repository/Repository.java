package edu.scasp.core.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic storage contract for an entity T identified by a key K. Stage 1 implements it with collections;
 * Stage 5 implements the same contract with JDBC, so the services above do not change.
 * Results are copies: callers cannot modify the repository's internal collections.
 */
public interface Repository<K, T> {

    /** Adds the entity, or replaces the one with the same key. */
    T save(T entity);

    Optional<T> findById(K id);

    List<T> findAll();

    boolean existsById(K id);

    boolean deleteById(K id);

    int count();
}
