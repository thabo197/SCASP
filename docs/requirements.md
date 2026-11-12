# Requirements Catalogue

Priority uses MoSCoW: **Must**, **Should**, **Could**. "Stage" shows where the requirement is built or extended.

## Functional requirements

| ID | Requirement | Priority | Stage | How verified |
|---|---|---|---|---|
| FR-01 | The system holds student and staff demo accounts, and each role can use only the functions it is permitted | Must | 1, 6 | Role tests, direct unauthorised access tests |
| FR-02 | Staff can create, update, search, sort and display courses with code, title, capacity and status | Must | 1, 5, 6, 7 | Unit tests, web tests |
| FR-03 | A student can register for an open course, and a duplicate registration is rejected | Must | 1, 2, 5 | Unit and concurrent tests |
| FR-04 | Registrations never exceed a course's capacity, even when requests arrive at the same time | Must | 1, 2 | Latch-based concurrency tests |
| FR-05 | When a course is full, a student can join its waiting list, ordered first in, first out (priority only where justified) | Must | 1, 5 | Ordering tests |
| FR-06 | A student can withdraw, and the next eligible waiting student is promoted automatically | Must | 1, 2, 5 | Scenario test |
| FR-07 | A student can submit a service request (transcript, clearance or appeal enquiry) | Must | 1, 5, 6, 7, 10 | Unit tests, web and mobile tests |
| FR-08 | Staff can prioritise, assign and track requests through valid status transitions | Must | 1, 5, 6 | Transition tests |
| FR-09 | Staff can publish announcements for defined audiences, and users retrieve those meant for them | Should | 1, 3, 5, 7, 10 | Audience filtering tests |
| FR-10 | Registration and request events generate notifications that are delivered, or simulated, over the network | Should | 3, 4 | Network tests, message trace |
| FR-11 | The system produces capacity, waiting-list, request-workload and student-registration reports | Must | 1, 5, 6, 7, 8 | Report tests against sample data |
| FR-12 | Important state changes are recorded in an audit trail with actor, action and time | Must | 1, 5 | Audit tests inside transactions |
| FR-13 | Selected campus services are listed with name, description, latitude and longitude, and the Android client opens a location or map view | Should | 5, 10 | Mobile tests |
| FR-14 | An administrator can run read and administrative operations through a separate Java client over RMI | Should | 8 | Failure and remote tests |
| FR-15 | An Android client supports login, course catalogue, registrations, service requests and campus services | Should | 10 | Mobile test scenarios |
| FR-16 | A TCP gateway accepts at least four documented commands from several clients at the same time | Must | 3 | Protocol tests (malformed, disconnect, timeout, shutdown) |

## Non-functional requirements

| ID | Requirement | Priority | Stage | How verified |
|---|---|---|---|---|
| NFR-01 | Security: prepared statements for all user-controlled SQL values, validated input, role checks on every protected operation, protected sessions, and no credentials in Git | Must | 5, 6, 7, 11 | Unauthorised access and injection-attempt tests, repository review |
| NFR-02 | Correctness: domain invariants are written down and covered by tests | Must | 1 onward | Test report, invariant list |
| NFR-03 | Concurrency safety: results do not depend on thread timing, and there are no lost, duplicated or over-capacity registrations | Must | 2 | Deterministic concurrent tests, race-condition evidence |
| NFR-04 | Reliability: timeouts, exception handling, resource cleanup, orderly shutdown and user-safe error messages | Must | 2, 3, 5, 6, 8 | Failure tests |
| NFR-05 | Data integrity: multi-step changes (registration, waiting list, audit) are atomic and roll back on failure | Must | 4, 5 | Commit and rollback tests |
| NFR-06 | Testability: automated unit and integration tests plus recorded manual tests for the interface and deployment | Must | All | Test report |
| NFR-07 | Maintainability: interfaces at module boundaries, small cohesive classes, and each business rule written once | Must | All | Code review |
| NFR-08 | Buildability: the project builds from documented instructions on each member's machine, with declared dependencies and no machine-specific paths | Must | 0, 11 | Clean build on each member's computer |
| NFR-09 | Usability and accessibility: responsive, accessible navigation and clear empty and error states on web and mobile | Should | 7, 10 | Usability test with two outside students |
| NFR-10 | Privacy: only fictional sample data, with no real student records | Must | All | Data and repository review |
| NFR-11 | Documentation: another group can build, run and understand the system from the submitted documents | Must | 11 | Fresh-machine walkthrough |
| NFR-12 | Responsiveness under load: bounded queues and back-pressure keep the system responsive instead of exhausting resources | Should | 2, 3 | Load and rejection tests |
