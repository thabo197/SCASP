# Stage 1 Demonstration Output

Command:

```
java -cp core-domain/target/classes edu.scasp.core.demo.Stage1Demo
```

This output was produced in an assistant-provided build environment. **Run it yourselves and replace it with your own output and screenshots** for the Stage 1 evidence.

```

== 1. Sample data loaded ==
8 users, 6 students, 5 courses, 3 campus services
Catalogue sorted by title:
  BCS301  Advanced Java  capacity 3  OPEN
  BCS102  Data Structures  capacity 2  OPEN
  BCS201  Databases  capacity 2  OPEN
  BCS101  Introduction to Programming  capacity 3  OPEN
  BCS999  Legacy Systems Seminar  capacity 1  CLOSED

== 2. Fill BCS101 (capacity 3), then waiting-list the rest ==
S001 -> REGISTERED
S002 -> REGISTERED
S003 -> REGISTERED
S004 -> WAITLISTED at position 1
S005 -> WAITLISTED at position 2
S006 -> WAITLISTED at position 3
Waiting list BCS101: [S004, S005, S006]; registered: [S001, S002, S003]

== 3. Duplicate and invalid operations are rejected ==
S001 registers for BCS101 again -> rejected: Student S001 is already registered for BCS101
S001 registers for the closed course BCS999 -> rejected: Course BCS999 is not open for registration
S002 tries to register S003 (a student acting for someone else) -> rejected: U002 may not act for student S003

== 4. S002 withdraws: the first waiting student is promoted ==
S002 withdrew; promoted: [S004]
Waiting list BCS101: [S005, S006]; registered: [S001, S003, S004]

== 5. S005 is suspended, so the next withdrawal skips them and promotes S006 ==
S001 withdrew; promoted: [S006]
Waiting list BCS101: [S005]; registered: [S003, S004, S006]

== 6. Service requests come out of the priority queue most urgent first ==
Queue: [REQ-0002(URGENT), REQ-0004(URGENT), REQ-0003(HIGH), REQ-0001(NORMAL)]
Assigned next: REQ-0002 URGENT APPEAL_ENQUIRY to U-STAFF1
Assigned next: REQ-0004 URGENT OTHER to U-STAFF1

== 7. Reports ==
Capacity:
  BCS101  Introduction to Programming    OPEN      capacity 3, registered 3, available 0, waiting 1
  BCS102  Data Structures                OPEN      capacity 2, registered 0, available 2, waiting 0
  BCS201  Databases                      OPEN      capacity 2, registered 0, available 2, waiting 0
  BCS301  Advanced Java                  OPEN      capacity 3, registered 0, available 3, waiting 0
  BCS999  Legacy Systems Seminar         CLOSED    capacity 1, registered 0, available 1, waiting 0
Waiting lists: [WaitlistRow[courseCode=BCS101, title=Introduction to Programming, studentIds=[S005]]]
Request workload: RequestWorkloadReport[byStatus={SUBMITTED=2, ASSIGNED=2, IN_PROGRESS=0, RESOLVED=0, CLOSED=0, REJECTED=0}, byCategory={TRANSCRIPT=1, CLEARANCE=1, APPEAL_ENQUIRY=1, OTHER=1}, openByAssignee={U-STAFF1=2}, unassigned=2]
Registrations per course: IntSummaryStatistics{count=5, sum=3, min=0, average=0.600000, max=3}
Audit events recorded: 17
  U006 SUBMIT_REQUEST ServiceRequest REQ-0003
  U003 SUBMIT_REQUEST ServiceRequest REQ-0004
  U-STAFF1 ASSIGN_REQUEST ServiceRequest REQ-0002
  U-STAFF1 ASSIGN_REQUEST ServiceRequest REQ-0004
  U-STAFF1 PUBLISH_ANNOUNCEMENT Announcement ANN-0001
```
