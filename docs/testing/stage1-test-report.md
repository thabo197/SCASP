# Stage 1 Test Report

## Summary

| Item | Result |
|---|---|
| Unit tests | 113 |
| Passed | 113 |
| Failed | 0 |
| Test framework | JUnit Jupiter |
| Scope | Model, repositories, services, reports and the Stage 1 demonstration scenario |

These 113 tests were first run in an assistant-provided build environment using the JUnit 5 console launcher. **Run `mvn clean verify` yourselves and record your own result here** (with the date, JDK and Maven versions). With the Stage 0 tests, Maven should report 115 tests.

| Our run | Value |
|---|---|
| Date | |
| JDK | |
| Maven | |
| Tests run / failures | |

## Tests by class

| Test class | Tests | Covers |
|---|---|---|
| CourseTest | 10 | Validation, equals/hashCode, immutability, natural order, comparators, status transitions |
| RegistrationTest | 4 | Equality ignores status and time, Set rejects duplicates |
| ServiceRequestTest | 10 | Priority ordering, PriorityQueue order, lifecycle, illegal transitions, comparators |
| EntityValidationTest | 9 | User, Student, WaitlistEntry, CampusService, Announcement, AuditEvent rules |
| InMemoryRepositoriesTest | 12 | Map ordering, copies, Set duplicates, FIFO waiting lists, skipping, removal from the middle |
| RegistrationServiceTest | 25 | Register, waiting list, capacity, duplicates, closed courses, authorisation, withdrawal, promotion, audit |
| CourseCatalogueServiceTest | 16 | Create, update, status, search, sorting, capacity rules, promotion on reopening or growth |
| ServiceDeskServiceTest | 13 | Submit, priority queue, assignment, lifecycle, invalid changes, listings, queue rebuild |
| AnnouncementServiceTest | 4 | Publishing, audience filtering, order |
| ReportServiceTest | 7 | Capacity, waiting list, workload, student and statistics reports |
| SequentialIdGeneratorTest | 1 | Identifier format and sequence |
| Stage1ScenarioTest | 2 | Sample data, and the minimum demonstration as a test |

## Invariant coverage

| ID | Invariant | Example tests |
|---|---|---|
| I-01 | One active registration per student and course | `register_sameStudentTwice_isRejected`, `aSet_cannotHoldTheSameStudentAndCourseTwice`, `registrationRepository_rejectsADuplicateStudentAndCourse` |
| I-02 | Registrations never exceed capacity | `register_neverExceedsCapacity`, `updateCourse_cannotReduceCapacityBelowTheRegisteredStudents` |
| I-03 | Not both registered and waiting | `register_whileAlreadyWaiting_isRejected` |
| I-04 | First-in, first-out waiting list | `waitlist_isFirstInFirstOut`, `register_whenFull_addsStudentsToTheWaitingListInOrder` |
| I-05 | A freed seat goes to the next eligible waiting student only | `withdraw_promotesTheFirstWaitingStudent`, `withdraw_skipsAnIneligibleStudentAndKeepsTheirPlace`, `withdraw_whenNobodyEligibleIsWaiting_leavesTheSeatFree` |
| I-06 | Stable, unique identifiers | `changes_returnNewObjectsAndLeaveTheOriginalUnchanged`, `ids_areSequentialPerPrefix_andStable`, equality tests |
| I-07 | Valid status transitions only | `illegalTransitions_areRejected`, `invalidStatusChange_isRejected`, `invalidStatusChanges_areRejected_andLeaveTheRequestUnchanged` |
| I-08 | Authorised operations only | `studentMayNotActForAnotherStudent`, `aStudentCannotCreateACourse`, `assign_requiresStaff_andAnActiveStaffAssignee` |
| I-09 | Every state change is audited | `everyStateChange_isRecordedWithActorActionAndTime`, `courseChanges_areAudited`, `failedOperations_leaveNoAuditEvent` |
| I-10 | Returned collections cannot change internal state | `queryResults_cannotBeUsedToChangeTheService`, `findAll_returnsAnUnmodifiableCopy`, `reportObjects_cannotBeModified` |
| I-11 | Positive capacity | `nonPositiveCapacity_isRejected` |

## Do the tests catch mistakes?

Three deliberate faults were introduced one at a time to check the tests would notice, and each was caught:

| Fault | Tests that failed |
|---|---|
| Waiting-list promotion ignores whether the student is eligible | 3 |
| Capacity check is off by one (allows one extra student) | 16 |
| Registration equality also compares status | 1 |

## Not covered in Stage 1

Concurrent use is deliberately not covered. The collections and services are single-threaded in this version, and Stage 2 adds the synchronisation and the concurrency tests.
