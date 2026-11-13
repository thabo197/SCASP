package edu.scasp.core.repository.memory;

import edu.scasp.core.repository.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Generic Map-backed repository. The Map implementation is chosen by each subclass: LinkedHashMap keeps
 * insertion order, TreeMap keeps keys sorted.
 */
public class InMemoryRepository<K, T> implements Repository<K, T> {

    private final Map<K, T> store;
    private final Function<T, K> keyOf;

    protected InMemoryRepository(Map<K, T> store, Function<T, K> keyOf) {
        this.store = store;
        this.keyOf = keyOf;
    }

    @Override
    public T save(T entity) {
        store.put(keyOf.apply(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(K id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public boolean existsById(K id) {
        return store.containsKey(id);
    }

    @Override
    public boolean deleteById(K id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() {
        return store.size();
    }
}
