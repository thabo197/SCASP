# SCASP: Smart Campus Academic Services Platform

BoCS3100 Advanced Java group project. One evolving system, built stage by stage: collections, concurrency, networking, enterprise beans, JDBC, servlets, JSP, RMI, a CORBA migration study and an Android client.

## Team

| Member | GitHub account | Starting role |
|---|---|---|
| (name) | (account) | Architecture and Integration Lead |
| (name) | (account) | Backend and Data Lead |
| (name) | (account) | Client, Quality and Documentation Lead |

Roles rotate after Stages 3 and 7. See `CONTRIBUTING.md`.

## Stage progress

| Stage | Focus | Tag | Status |
|---|---|---|---|
| 0 | Inception and engineering baseline | v0.1 | In progress |
| 1 | Collections | v1.0 | Not started |
| 2 | Multithreading | v2.0 | Not started |
| 3 | Networking | v3.0 | Not started |
| 4 | Enterprise beans | v4.0 | Not started |
| 5 | JDBC | v5.0 | Not started |
| 6 | Servlets | v6.0 | Not started |
| 7 | JSP / Jakarta Pages | v7.0 | Not started |
| 8 | RMI | v8.0 | Not started |
| 9 | CORBA migration dossier | v9.0 | Not started |
| 10 | Android client | v10.0 | Not started |
| 11 | Integration and final release | v11.0 | Not started |

## Prerequisites, build, database, deployment and run instructions

Added when the Maven baseline is committed (Stage 0) and extended at each stage.

## Repository layout

```
core-domain/             entities, interfaces, validation, collection-based repositories
concurrent-processing/   executors, queues, shutdown, statistics
network-gateway/         TCP/UDP protocol, client and server
enterprise-services/     enterprise beans
persistence-jdbc/        JDBC repositories
web-application/         servlets, filters, JSP views
rmi-administration/      RMI interfaces, server, client
android-client/          Android Studio project
database/                SQL schema and sample-data scripts
docs/                    architecture, protocols, CORBA dossier, testing, user guide
```

## Acknowledgements

External libraries and AI assistance are acknowledged in `docs/ACKNOWLEDGEMENTS.md`.
