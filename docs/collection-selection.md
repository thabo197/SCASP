# Stage 1: Collection-Selection Table

Each collection was chosen to express a domain rule directly, so the rule is enforced by the data structure and not by extra checking code.

## Collections used

| Need | Collection | Where | Why this one | Alternatives not chosen |
|---|---|---|---|---|
| Users, students, announcements, requests and campus services, looked up by unique id | `LinkedHashMap<String, T>` | `InMemoryRepository` and its subclasses | Constant-time lookup by id; keeps insertion order, so listings and reports are predictable | `HashMap`: arbitrary order. `TreeMap`: sorting cost with no benefit here |
| Courses, listed in course-code order | `TreeMap<String, Course>` | `InMemoryCourseRepository` | Keys stay sorted, so the catalogue is always in code order without sorting each time | `HashMap` would need a sort on every listing |
| Active registrations | `LinkedHashSet<Registration>` | `InMemoryRegistrationRepository` | `Registration.equals` and `hashCode` use (studentId, courseCode), so a duplicate cannot be added (invariant I-01); `add` reports whether it was new | `List`: allows duplicates and needs a linear scan to check |
| Waiting list for each course | `ArrayDeque<WaitlistEntry>` per course, held in a `TreeMap` | `InMemoryWaitlistRepository` | First in, first out is fair; `addLast` and removal are constant time; an iterator lets us remove from the middle to skip an ineligible student | `PriorityQueue`: no priority rule is justified for seats, so it would only add unfairness. `LinkedList`: no advantage |
| Service requests waiting to be assigned | `PriorityQueue<ServiceRequest>` | `DefaultServiceDeskService` | The natural order (highest priority, then oldest) makes `poll()` return the most urgent request; justified because urgency matters here | `TreeSet`: works but is not a queue. A sorted `List`: re-sorting on every insert |
| Audit events | `ArrayList<AuditEvent>` | `InMemoryAuditEventRepository` | Append-only, and the order of events is the order they happened | A `Set` would lose the order |
| Counts per status or category in reports | `EnumMap<K, Long>` | `DefaultReportService` | Compact and fast for enum keys, and iterates in enum order | `HashMap` |
| Open requests per staff member | `TreeMap<String, Long>` | `DefaultReportService` | Sorted by staff id for a stable report | `HashMap` |
| Identifier counters | `ConcurrentHashMap<String, AtomicLong>` | `SequentialIdGenerator` | Thread-safe, so it is ready for the Stage 2 workers | `HashMap` with a plain `long` |

## Equality and ordering

| Type | Identity (`equals` / `hashCode`) | Natural order (`Comparable`) | Extra `Comparator` strategies |
|---|---|---|---|
| Course | course code | course code | by title, by capacity (largest first), by status then code |
| ServiceRequest | request id | highest priority, then oldest, then id | by created time, by category then created, by status then priority |
| Student | student id | student id | |
| Registration, WaitlistEntry | student id and course code | | |
| User, Announcement, AuditEvent, CampusService | their own id | | |

Entities are compared by identity (their id), not by every field, so a `Set` or `Map` treats two versions of the same entity as the same thing. The tests check this, including that a Registration with a different status or timestamp is still a duplicate.

## Collection algorithms, streams and collectors

| Purpose | Technique |
|---|---|
| Course and request listings | `sorted(Comparator)`, `Collections.sort` with `Comparable` |
| Course search and status filters | `Stream.filter` with case-insensitive matching |
| Registrations or waiting students per course | `Collectors.groupingBy` with `Collectors.counting()` |
| Open requests per staff member | `groupingBy` with a `TreeMap` factory and `counting()` |
| Courses registered per student | `groupingBy` with `Collectors.mapping` and `toList()` |
| Registrations-per-course summary | `Collectors.summarizingInt` (count, sum, min, max, average) |

## Defensive copies at service boundaries

Repositories and services return `List.copyOf` or an unmodifiable set, so a caller cannot change internal state (invariant I-10). Domain objects are immutable records, and every change returns a new object. Tests confirm that modifying a returned collection throws `UnsupportedOperationException` and leaves the original untouched.
