# Risk Register

Likelihood and impact: **L** low, **M** medium, **H** high. Review at the start of every stage and update the status.

| ID | Risk | Likelihood | Impact | Mitigation | Owner | Status |
|---|---|---|---|---|---|---|
| R01 | Integration: modules that work alone fail when combined | M | H | Keep repository and service interfaces in `core-domain`, run integration tests every stage, tag each stage, do a clean-install check on another machine early | (member) | Open |
| R02 | Obsolete technology: CORBA and entity beans are no longer part of the JDK or the platform, so there is little tooling | H | L | Treat Stage 9 as a documentation study as the brief allows, and explain the persistence replacement for entity beans | (member) | Open |
| R03 | Team availability: a member is unavailable, or the work is concentrated on one person | M | H | Split work into small issues, review through pull requests, keep the contribution log accurate, write a short explainer per stage, raise problems with the lecturer early | (member) | Open |
| R04 | Schedule: twelve stages must fit a short period | H | H | Target one stage per day, build Must items first, keep a buffer day, and keep a list of items that can be trimmed | (member) | Open |
| R05 | Runtime and deployment: the Jakarta EE server or EJB deployment fails or differs between machines | M | H | Confirm the runtime with the lecturer in Stage 0, set it up before Stage 4, document every step, keep deployment evidence | (member) | Open |
| R06 | Concurrency defects are hard to reproduce and make tests flaky | M | M | Use latches and barriers instead of sleeps, capture the deliberate race condition and a thread dump | (member) | Open |
| R07 | Security weaknesses such as SQL injection or broken access control | M | H | Use prepared statements only, enforce roles in a filter, test unauthorised access | (member) | Open |
| R08 | Android to server connectivity (emulator networking, plain HTTP restrictions) | M | M | Use the emulator's host address, a development network configuration, and the controlled mock the brief allows as a fallback | (member) | Open |
| R09 | Generated or reused code is defective or not understood by the member submitting it | M | H | Review all code in pull requests, run the tests, write stage explainers, acknowledge assistance, and make sure each member can explain a design decision and a code segment | (member) | Open |
| R10 | Code loss or secrets committed to Git | L | H | Use the remote repository, protected `main`, `.gitignore`, and the no-secrets rule in reviews | (member) | Open |
| R11 | Scope creep adds work without adding marks | M | M | Changes to scope need lecturer approval and go on a backlog | (member) | Open |
| R12 | Environment mismatch (JDK or Maven versions differ between machines) | M | M | Pin the Java release and plugin versions in the parent POM, commit the Maven wrapper, document versions | (member) | Open |
| R13 | The Stage 7 usability test needs two students outside the group, who may be hard to find | M | L | Arrange participants early and prepare a short test script | (member) | Open |
