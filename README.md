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

# Testing

Some cool things worth checking out:

1. `ClientsPersistenceSpec` – intercepts logs to see if Hibernate cache works.
1. `BankAccountSpec` – no dependencies other than on itself except some stable infra code. Signals a good test.
1. `BankAccountSpec` – looks like an executable specification (⌘ + F12 to see the test list).
1. `BankAccountPersistenceAndLockingSpec` - demonstrates the importance of optimistic locking.
1. `ValidatorSpec` - data-driven tests
1. `ArchitectureSpec` - architectural constraints
1. `UsefulTestExtensions` - adds convenience testing methods
1. `TimeMachine` - thread-local clock, allowing per-test time manipulation.

All other tests are end-to-end, covering the application from Web API to database. This approach is far more valuable
than having numerous unit tests per use case (controller, command, reaction, entities, repository...) that neither
ensure functionality nor provide a safety net for internal restructuring. We create a beautiful E2E testing API around
Web API to make tests more awesome.
See `BankAccountE2ESpec` and `IpRateLimitingE2ESpec`.

More awesomeness:

1. WebMvc doesn't spin up a Http server (fast ⚡).
1. E2E tests don't reuse data classes (commands and responses) making tests more robust. If you rename a field in data
   structure, essentially breaking an API, a test should fail, not pass.
1. E2E tests are designed to run in parallel (thanks to Faker) using in-memory H2, eliminating DB truncation / context
   dirtying / rollbacks.
1. E2E tests use realistic transaction(s), like a normal app. It doesn't run in a single transaction tx that spans the
   whole test (forcing commands to join that single tx, which is a source of bugs, because IRL every command runs in its
   own tx).
1. H2 supports some advanced SQL features (such as SKIP LOCKED, which we rely on to implement a DB-backed queue). If
   your app relies on Postgres functionality, you can isolate these tests and run them against real Postgres (using
   Postgres Templates or TestContainers). Alternatively, you can simulate some of the advanced PostgreSQL
   functionality (like pg_notify() or pg_advisory_lock()) in H2. While this won’t be completely bulletproof, as your
   local app will behave slightly differently, it’s a worthwhile trade-off that enables you to leverage an embedded
   database as much as possible for testing and local development. Refer to `AdvisoryLock.java` for an example.

### A note on testing strategy

My testing strategy is simple – E2E tests are mandatory, since the use cases our app provides via web api should be kept
**stable**. Everything else is there to support the use-cases and is **volatile**; internals can and will regularly
change as a result of refactoring and restructuring. As long as use cases keep working – E2E tests should pass. Good
tests shall not be coupled to implementation details. Since what we expose is Web API, we should exercise the app via
the Web API.

Unit tests target a specific code unit—a domain object, a controller, a repository. Some classes deserve a unit test;
others don’t, and the decision should be made case-by-case. As with any additional code, trade-offs must be considered
since writing and maintaining unit tests incurs costs. Assuming that E2E tests are mandatory and all observable
functionality is covered, a unit test should add value to the test suite, with benefits outweighing its maintenance
costs.

For example, `BankAccount` is an ideal candidate for unit testing because it’s a self-contained unit that consolidates
non-trivial logic and invariants. Testing these in isolation provides a faster safety net and turns the test into an
executable specification for that unit. It’s a useful, low-overhead, addition to the test suite.

On the other hand, unit-testing `Client` doesn’t add value, as it’s just a data class with no logic (remember: it’s
already covered by E2E tests). If Client later gains behavior and becomes an object, then creating unit tests could be
worthwhile. But for now, YAGNI.

I don’t buy into the "fully isolated unit tests" craze that leads to over-mocking and fragile, useless tests. A
repository is better tested via an in-memory database. A controller is better tested through WebMVC to verify (de)
serialization and exception handling. A class that interacts with the file system should use an in-memory file system (
e.g., Jimfs). A class that sends emails via SMTP should use an in-memory SMTP server (e.g., GreenMail). A class that
interacts with an external system should use a test double (e.g., WireMock). A domain object, if well-designed, can
usually be tested in full isolation without running Spring.

Write **valuable tests**. Valuable tests are worth optimizing. Optimize them by buying better hardware, parallelization,
ditching low-value tests. It’s better to have fewer valuable tests than many useless tests.

P.S. Whether you test first or test last doesn't matter. What matters is good design paired with good tests. How you get
there is up to you.
