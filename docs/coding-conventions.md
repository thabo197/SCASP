# Coding Conventions

## Language and build

- Java 21 or newer, set once in the parent `pom.xml`. Maven builds every module.
- Package root: `edu.scasp.<module>`, for example `edu.scasp.core`.
- Each module has its own `pom.xml`. Dependencies are declared, and versions are managed in the parent.

## Style

- Four-space indentation, one top-level class per file, lines up to 120 characters.
- Classes use `UpperCamelCase`, methods and fields `lowerCamelCase`, constants `UPPER_SNAKE_CASE`.
- Names say what something is or does. Avoid abbreviations.
- Prefer small, cohesive classes and methods. Write each business rule once.
- Prefer immutable objects: `final` fields, no setters unless needed.
- Put interfaces at module boundaries, and depend on interfaces, not implementations.
- Return unmodifiable or copied collections across service boundaries.

## Errors and resources

- Never swallow exceptions. Handle them, or wrap and rethrow with context.
- Close resources with try-with-resources.
- Validate input at the boundary and return user-safe messages.
- No credentials, keys or personal data in code or in Git.

## Tests

- JUnit with one test class per production class, named `<Class>Test`.
- Test method names read `method_condition_expectedResult`.
- Concurrency tests use latches or barriers, never long sleeps.
- Every domain invariant has at least one test, including the invalid case.

## Version control

Branches, commits, pull requests, reviews and tags follow `CONTRIBUTING.md`.
