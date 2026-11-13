package edu.scasp.core.repository.memory;

import edu.scasp.core.model.Course;
import edu.scasp.core.model.Registration;
import edu.scasp.core.repository.RegistrationRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A LinkedHashSet: Registration.equals/hashCode use (studentId, courseCode), so duplicates are impossible,
 * and insertion order is kept for stable output.
 */
public class InMemoryRegistrationRepository implements RegistrationRepository {

    private final Set<Registration> registrations = new LinkedHashSet<>();

    @Override
    public boolean add(Registration registration) {
        return registrations.add(registration);
    }

    @Override
    public Optional<Registration> remove(String studentId, String courseCode) {
        String code = Course.normalizeCode(courseCode);
        Optional<Registration> existing = registrations.stream()
                .filter(r -> r.studentId().equals(studentId) && r.courseCode().equals(code))
                .findFirst();
        existing.ifPresent(registrations::remove);
        return existing;
    }

    @Override
    public boolean exists(String studentId, String courseCode) {
        String code = Course.normalizeCode(courseCode);
        return registrations.stream()
                .anyMatch(r -> r.studentId().equals(studentId) && r.courseCode().equals(code));
    }

    @Override
    public int countByCourse(String courseCode) {
        String code = Course.normalizeCode(courseCode);
        return (int) registrations.stream().filter(r -> r.courseCode().equals(code)).count();
    }

    @Override
    public List<Registration> findByCourse(String courseCode) {
        String code = Course.normalizeCode(courseCode);
        return registrations.stream()
                .filter(r -> r.courseCode().equals(code))
                .sorted(Comparator.comparing(Registration::createdAt).thenComparing(Registration::studentId))
                .toList();
    }

    @Override
    public List<Registration> findByStudent(String studentId) {
        return registrations.stream()
                .filter(r -> r.studentId().equals(studentId))
                .sorted(Comparator.comparing(Registration::courseCode))
                .toList();
    }

    @Override
    public Set<Registration> findAll() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(registrations));
    }
}
