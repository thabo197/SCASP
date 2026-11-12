# Project Charter: Smart Campus Academic Services Platform (SCASP)

BoCS3100 Advanced Java comprehensive group project.

## 1. Problem definition

A higher-education institution needs one integrated platform for academic services. Students must be able to view courses, register for them, join a waiting list when a course is full, raise academic service requests (for example transcript, clearance or appeal enquiries), read announcements and find campus services. Staff must be able to manage courses, process and assign requests, monitor workload and produce reports. Web, desktop administration and mobile clients all need controlled access to the same services.

Without a single platform these tasks are handled separately, which makes capacity limits, waiting lists and request status hard to keep consistent. SCASP solves this by keeping the rules in one place (a reusable Java domain layer) and exposing them through concurrent processing, a network gateway, enterprise services, a relational database, a web application, a remote administration interface and an Android client.

### Intended users

| User | What they need |
|---|---|
| Student | Browse courses, register or withdraw, join a waiting list, submit and track service requests, read announcements, find campus services (web and Android) |
| Academic staff | Create and update courses, see registrations and waiting lists, publish announcements |
| Administrative staff | Prioritise, assign and resolve service requests, monitor workload, view reports |
| System administrator | Run read and administrative operations remotely through a separate Java client |

## 2. Goals and success criteria

- Deliver the twelve stages (0 to 11) as one evolving system, with a tagged version per stage.
- Every domain invariant (unique registration, capacity limit, valid status transitions, stable identifiers, authorised operations) is documented and tested.
- The system builds and runs from the documented instructions on each member's machine.
- All three members can explain a design decision and a code segment in the final demonstration.

## 3. Scope

**In scope:** the ten units of BoCS3100 applied to SCASP: collections, multithreading, networking, enterprise beans, JDBC, servlets, JSP, RMI, a CORBA migration study, and an Android client.

**Out of scope:** real student data, real authentication providers, payments, production hosting, and a full CORBA implementation (optional, only with a lecturer-provided environment).

## 4. Constraints and assumptions

- Language and platform: Java, Jakarta EE runtime, relational database, Android Studio.
- Jakarta EE runtime and database: to be confirmed with the lecturer. (Confirm before Stage 4.)
- Deadline: to be confirmed. (Record it here once confirmed.)
- Group of three. Sample data is fictional. No credentials or personal data in the repository.
- Generated assistance and external libraries are acknowledged in `docs/ACKNOWLEDGEMENTS.md`.

## 5. Team and role plan

| Member | GitHub account | Roles over the project |
|---|---|---|
| (name) | (account) | Stages 0 to 3: Architecture and Integration. Stages 4 to 7: Backend and Data. Stages 8 to 11: Client, Quality and Documentation |
| (name) | (account) | Stages 0 to 3: Backend and Data. Stages 4 to 7: Client, Quality and Documentation. Stages 8 to 11: Architecture and Integration |
| (name) | (account) | Stages 0 to 3: Client, Quality and Documentation. Stages 4 to 7: Architecture and Integration. Stages 8 to 11: Backend and Data |

Roles are leadership responsibility, not exclusive ownership, and all members write code. Roles rotate after Stages 3 and 7, so every member leads design, implementation and quality work.

## 6. Stage plan

| Stage | Focus | Output | Tag | Target date |
|---|---|---|---|---|
| 0 | Inception | Charter, requirements, architecture baseline, repository, build baseline, risk register | v0.1 | |
| 1 | Collections | In-memory domain core and tests | v1.0 | |
| 2 | Multithreading | Concurrent request processor | v2.0 | |
| 3 | Networking | TCP and UDP gateway | v3.0 | |
| 4 | Enterprise beans | Container-managed business services | v4.0 | |
| 5 | JDBC | Relational persistence and transactions | v5.0 | |
| 6 | Servlets | HTTP controllers, sessions, role checks | v6.0 | |
| 7 | JSP | Server-rendered interface | v7.0 | |
| 8 | RMI | Remote administration client | v8.0 | |
| 9 | CORBA | Interoperability and migration dossier | v9.0 | |
| 10 | Android | Mobile client and location feature | v10.0 | |
| 11 | Integration | Final tests, documentation, release | v11.0 | |

## 7. Definition of done for a stage

- Builds from the documented instructions and all tests pass.
- Documentation and README updated, and the stage deliverable evidence collected.
- Pull request reviewed and approved by a different member, then merged.
- Annotated tag created and pushed.
- Contribution log updated.
