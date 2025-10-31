# Test Documentation

## Overview

**Total: 44 tests** covering endpoints, services, repositories, and concurrency.

```
Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
```

## Test Structure

```
src/test/java/com/redhat/quarkus/donation/
├── rest/              # 8 integration tests (REST endpoints)
├── service/           # 18 unit tests (business logic)
├── repository/        # 10 unit tests (persistence)
└── concurrency/       # 8 concurrency tests (load & thread safety)
```

## Running Tests

```bash
# All tests
mvn test

# Specific class
mvn test -Dtest=DonationServiceTest

# Specific method
mvn test -Dtest=DonationServiceTest#testCreateDonation
```

## Test Configuration

**Database:** H2 in-memory (configured in `src/test/resources/application.properties`)

**Mocking:**
- PayPal external API mocked using `@InjectMock`
- Database operations use real H2 instance

## Key Test Files

| File | Tests | Purpose |
|------|-------|---------|
| `DonationResourceTest.java` | 8 | REST endpoint integration tests |
| `DonationServiceTest.java` | 9 | Donation service unit tests |
| `PayPalServiceTest.java` | 9 | PayPal service unit tests |
| `DonationRepositoryTest.java` | 10 | Repository CRUD tests |
| `ConcurrencyTest.java` | 8 | Concurrency & load tests |

## Dependencies

Required test dependencies in `pom.xml`:
- `quarkus-junit5`
- `rest-assured`
- `quarkus-junit5-mockito`
- `quarkus-jdbc-h2`
- `assertj-core`
- `awaitility`
