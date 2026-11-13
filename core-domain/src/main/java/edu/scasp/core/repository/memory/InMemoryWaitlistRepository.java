package edu.scasp.core.repository.memory;

import edu.scasp.core.model.Course;
import edu.scasp.core.model.WaitlistEntry;
import edu.scasp.core.repository.WaitlistRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

/** An ArrayDeque per course gives first-in, first-out order; iterating lets us remove from the middle. */
public class InMemoryWaitlistRepository implements WaitlistRepository {

    private final Map<String, Deque<WaitlistEntry>> queues = new TreeMap<>();

    @Override
    public boolean enqueue(WaitlistEntry entry) {
        if (contains(entry.studentId(), entry.courseCode())) {
            return false;
        }
        queues.computeIfAbsent(entry.courseCode(), code -> new ArrayDeque<>()).addLast(entry);
        return true;
    }

    @Override
    public boolean contains(String studentId, String courseCode) {
        return entries(courseCode).stream().anyMatch(e -> e.studentId().equals(studentId));
    }

    @Override
    public Optional<WaitlistEntry> remove(String studentId, String courseCode) {
        return pollFirstMatching(courseCode, e -> e.studentId().equals(studentId));
    }

    @Override
    public List<WaitlistEntry> entries(String courseCode) {
        Deque<WaitlistEntry> queue = queues.get(Course.normalizeCode(courseCode));
        return queue == null ? List.of() : List.copyOf(queue);
    }

    @Override
    public int size(String courseCode) {
        Deque<WaitlistEntry> queue = queues.get(Course.normalizeCode(courseCode));
        return queue == null ? 0 : queue.size();
    }

    @Override
    public Optional<WaitlistEntry> pollFirstMatching(String courseCode, Predicate<WaitlistEntry> eligible) {
        Deque<WaitlistEntry> queue = queues.get(Course.normalizeCode(courseCode));
        if (queue == null) {
            return Optional.empty();
        }
        Iterator<WaitlistEntry> it = queue.iterator();
        while (it.hasNext()) {
            WaitlistEntry candidate = it.next();
            if (eligible.test(candidate)) {
                it.remove();
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<WaitlistEntry> findAll() {
        List<WaitlistEntry> all = new ArrayList<>();
        queues.values().forEach(all::addAll);
        return List.copyOf(all);
    }
}
