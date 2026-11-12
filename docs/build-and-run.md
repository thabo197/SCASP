# Build and run (Stage 0 baseline)

## Prerequisites

- Git
- JDK 21 or newer (`java -version`)
- Maven 3.9 or newer (`mvn -version`), or use the Maven wrapper (`mvnw`, `mvnw.cmd`) once it is committed

## Build and test

From the repository root:

```
mvn clean verify
```

Expected: `BUILD SUCCESS` and 2 tests run in `scasp-core-domain`.

## Run the baseline

```
java -cp core-domain/target/classes edu.scasp.core.Main
```

Expected output (the Java version will match your JDK):

```
SCASP - Smart Campus Academic Services Platform
Java 21.0.x
```

## Version settings

The Java release, JUnit version and Surefire version are set once in the parent `pom.xml`. If your JDK is older than 21, change `maven.compiler.release` and tell the team, because Stage 2 uses virtual threads.
