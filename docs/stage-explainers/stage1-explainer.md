# Stage 1 Explainer: In-Memory Core

Read this before reviewing the Stage 1 pull request. Everyone should be able to answer the questions at the end.

## What Stage 1 builds

The business rules of the platform, with no database, threads or network yet. Everything lives in `core-domain`:

| Package | What it holds |
|---|---|
| `model` | Immutable records (Course, Student, Registration and so on), the status enums, comparators, `Validate` |
| `exception` | `ValidationException`, `EntityNotFoundException`, `DuplicateEntityException`, `InvalidOperationException`, `UnauthorizedOperationException` |
| `repository` | The `Repository<K, T>` interface and the specific repository interfaces. These are the contracts Stage 5 will implement with JDBC |
| `repository.memory` | The collection-based implementations (Map, Set, Deque) |
| `service` | The rules: catalogue, registration and waiting list, service desk, announcements, reports, access policy and audit trail. `Platform` wires them together |
| `demo` | `SampleData` (fictional) and `Stage1Demo` |

## How a registration works

1. `AccessPolicy` checks who is acting: a student may act only for themselves, and staff or admin for anyone.
2. The service loads the student and course, and rejects an inactive student, a course that is not open, and a duplicate registration or waiting-list entry.
3. If a seat is free, the student is added to the registration `Set`. If not, they go to the back of the course's waiting `Deque`.
4. Each change is written to the audit trail with the actor, action and time.
5. When someone withdraws, `SeatPromoter` offers the freed seat to the first waiting student who is eligible. A suspended student is skipped but keeps their place.

## Ideas worth knowing

- **Records are immutable.** A change returns a new object, so a value can never change underneath a collection that holds it.
- **Equality is by identity.** Two registrations are "the same" if the student and course match, whatever their status or timestamp. That is what makes the `Set` reject duplicates.
- **Natural order versus comparators.** `Comparable` defines the default order (courses by code, requests by priority then age). `Comparator` objects give other orders on demand.
- **The `PriorityQueue` is a derived index.** The map is the source of truth, and the queue is rebuilt from it when the service starts. Iterating a `PriorityQueue` is not sorted, so `pendingQueue()` sorts a copy.
- **Defensive copies.** Services return `List.copyOf` or unmodifiable sets, so callers cannot damage internal state.
- **One place for each rule.** Promotion lives in `SeatPromoter`, and permissions live in `AccessPolicy`, so no rule is written twice.

## Questions every member should be able to answer

1. Why is the registration store a `Set`, and what makes it reject duplicates?
2. Why is the waiting list a `Deque` rather than a `PriorityQueue`?
3. What does `pollFirstMatching` do, and why does a suspended student keep their place in the queue?
4. Why do `Course` and `ServiceRequest` implement `Comparable`, and where are `Comparator` strategies used?
5. Why are the domain objects immutable records?
6. What would happen if `Registration.equals` also compared the status?
7. How do you know a returned list cannot be used to change the repository?
8. Why does `Platform` take a `Repositories` object, and what will Stage 5 change?
9. Which collector gives the count of registrations per course?
10. Why is Stage 1 not yet safe for several threads?

## Short answers

1. A `LinkedHashSet` with `equals` and `hashCode` on (studentId, courseCode); `add` returns false for a duplicate.
2. Seats go in the order students asked. A priority queue would add a ranking that nothing in the requirements justifies.
3. It removes and returns the first entry that passes the eligibility test, leaving the others in order. Skipping must not cost the student their place.
4. `Comparable` is the default order. Comparators give titles, capacity and status orderings for listings.
5. A change returns a new object, so shared values cannot be altered by accident.
6. A withdrawn and an active registration for the same student and course would count as different, and the Set would allow a duplicate.
7. Repositories return copies (`List.copyOf`, unmodifiable sets), and tests confirm that changing them throws `UnsupportedOperationException`.
8. So the services do not know how data is stored. Stage 5 passes a JDBC-backed `Repositories` and the services stay the same.
9. `Collectors.groupingBy(Registration::courseCode, Collectors.counting())`.
10. The collections are not thread-safe and the check-then-add steps can interleave. Stage 2 protects them.

## Suggested split for explaining it in the demo

| Member | Explains |
|---|---|
| Member 1 | The model and the collection-selection table |
| Member 2 | Registration, waiting list and promotion |
| Member 3 | The tests, the invariant table and the demo scenario |
