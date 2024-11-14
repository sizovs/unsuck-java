# Clean Bank

Complementary materials for the course **[Clean Code: The Next Level](https://cleancode.training)**. The codebase
demonstrates some unique and practical coding techniques and patterns.

### Requirements

- [x] Java 23+
- [x] Gradle 8+

#### How to run tests?

```
gradle test
```

#### How to run dev mode?

```
gradle bootTestRun
```

### Some cool things about testing
1. `ClientsPersistenceSpec` – intercepts logs to see if Hibernate cache works.
1. `BankAccountSpec` – no dependencies other than on itself except some stable infra code. Signals a good test.
1. `BankAccountSpec` – looks like an executable specification (⌘ + F12 to see the test list).
1. `BankAccountPersistenceAndLockingSpec` - demonstrates the importance of optimistic locking.
1. `ValidatorSpec` - data-driven tests
1. `DataSpec` - equals() and hashcode() tests
1. `ArchitectureSpec` - architectural constraints


All other tests are end-to-end, covering the application from Web API to database. This approach is far more valuable than having numerous unit tests per use case (controller, command, reaction, entities, repository) that neither ensure functionality nor provide a refactoring safety net for internal restructuring. We create a beautiful E2E testing API around Web API to make tests more awesome.
See BankAccountE2ESpec and IpRateLimitingE2ESpec.

Cool things:
1. WebMvc doesn't spin up a Http server (fast ⚡).
1. E2E tests don't reuse data classes (commands and responses) making tests more robust. If you rename a field in data structure, essentially breaking an API, a test should fail, not pass.
1. E2E tests are designed to run in parallel (thanks to Faker) using in-memory H2, eliminating DB truncation / context dirtying / rollbacks.
1. E2E tests use realistic transaction(s), like a normal app. It doesn't run in a single transaction tx that spans the whole test (forcing commands to join that single tx, which is a source of bugs, because IRL every command runs in its own tx).
1. H2 supports some advanced SQL features (such as SKIP LOCKED, which we rely on to implement a DB-backed queue).If your app relies on Postgres functionality, you can isolate these tests and run them against real Postgres (using Postgres Templates or TestContainers).
