# Tasks

Check out the branch `start-mock-tests` and start writing your test code there. Don't look at the `master` branch as it contains the solution and would spoil all the fun. :-)

## Mini Hands-On Part 1: Basic Setup and Naming

- `ApplicationDAO`
  - Tip: With `PostgreSQLInstance.jdbi` you can create a `Jdbi` instance which can be passed to the `ApplicationDAO`. The `TestDAO` contains useful functions to create the schema and fill the table with test data. It can be created with `PostgreSQLInstance.jdbi.onDemand<TestDAO>()`.
  - **Test**: Filtering by `ApplicationState` should only return the applications with the requested state.

## Mini Hands-On Part 2: Basic Setup with Mocks

- `ApplicationController`
  - Tip: Check out the file `util/MockMvcUtils.kt` in the test folder. It contains many useful functions to use Spring's MockMvc API. For instance, you can use `createMockMvc(controller)` to create an `mvc` object for sending HTTP requests against the controller. Also, `mvc.requestApplications()` is useful to send a GET request to the controller.
  - Tip: Check out the dependencies of `ApplicationController`: The `Clock`, the `AddressValidationClient` and the `ApplicationDAO` should be mocked. For the `mapper` you can use `TestObjects.mapper`.
  - **Test**: List applications (`GET /applications`)
    - A `GET` request on `/applications` should return a list of JSON documents with the fields `id`, `fullName`, `jobTitle`, `state` and `dateCreated` from the database.

## Hands-On Part 3: Mock-Based Unit Tests

- `ApplicationDAO`
  - **Test**: Return all applications if no state is requested.
  - **Test**: Order applications by `dateCreated`.
- `AddressValidationClient`
  - Tip: Check out the file `util/MockServerUtils.kt` in the test folder. It contains many useful functions to use the WebMockServer API.
  - **Test**: Pass a 200 response to the caller with a success object.
  - **Test**: Return an error object if the validation service returns a 500.
- `ApplicationController`
  - Create an application (`POST /applications`)
    - **Test**: A `POST` request on `/applications` (containing an application as JSON in the body) creates an application and an applicant entry in the database with the posted values and the current timestamp.
    - **Test**: Reject application creation when the AddressValidationService says that the submitted address is invalid.
    - **Test**: Return a 500 status code if the request to the AddressValidationService was not successfully

## Hands-On Part 4: Integration Tests

You can check out the branch `start-integration-tests` and start coding there. It contains the solution of the previous tasks.

- Migrate some tests from part 1 to the integration test `ApplicationControllerITest`. For this, wire the real objects together (not mocks) and test all layers at once (Controller, DAO, Client). Only migrate those tests that describe behavior of the service that is visible outside of it (ingoing requests, changed databases entries, outgoing responses). Internals (like internal data structures, exceptions, outcome objects) are implementation details and not relevant. Only the resulting behavior of those internals are relevant.
- **Test**: When `GET`ing an application, its attachments should be returned (as pairs of file name and file path) if the database entry contains attachments.
  - e.g. the string `{"letter": "path/to/letter.pdf", "cv": "path/to/cv.pdf"}` in the database column `attachments` should be returned in the HTTP JSON payload.
  - A `null` in the database should be mapped to an empty map in the JSON payload.
- **Test**: Don't create an application and return a 400 if an required JSON field is missing. Test this with all fields (because all fields are required).
- **Test**: Don't create an application and return a 400 if an invalid JSON is passed. Try at least the strings "", "asdf", "2", "{}", "[]".

## Hands-On Part 5: Kotest, Table-Driven Testing, Property-Based Testing

You can check out the branch `start-kotest` and start coding there. It contains the solution of the previous tasks.

- Let's migrate our test suite `ApplicationControllerITest` from JUnit5 to Kotest (name: `ApplicationControllerKoTest`).
  - A good starting point is the [`FreeSpec` testing style](https://kotest.io/docs/framework/testing-styles.html#free-spec). It supports grouping which can be used instead of JUnit5's `@Nested`.
  - First, create the fixture setup und grouping in Kotest. Examples can be found in the `com.phauer.krecruiter.kotest` test package.
  - Second, migrate 2 or more tests
- Use [data-driven testing instead of parameterized tests](https://kotest.io/docs/framework/datatesting/data_driven_testing_4.2.0). The tests that are checking for a 400 response are good candidates.
- Write a [property-based test](https://kotest.io/docs/proptest/property-based-testing.html) that generates random values for the application fields and create an application with this data.
  - You can rewrite an existing test for this.
  - Hint: Unfortunately, property-based tests and table-driven tests don't call the defined `beforeTest()` method. So you have to call it manually with `beforeTest(mockk<TestCase>())` before each iteration.

## Bonus Tasks

Choose one or multiple tasks from the following ones:

- Read about Kotest's [Inspectors](https://kotest.io/docs/assertions/inspectors.html)
  or [Custom Matcher](https://kotest.io/docs/assertions/assertions.html#custom-matchers) and try to apply them.
- Make your test code even cleaner, expressive and understandable.
  - Read briefly through [Modern Best Practices for Testing in Java](https://phauer.com/2019/modern-best-practices-testing-java/). Although the examples are written in Java, the described best practices also apply to Kotlin test code.
  - Are there any recommendations that can improve your test code? Try to refactor your code.
- Test that a correct `location` header is returned in the response for creating an application. It should look like `/applications/<applicationId>`.
- Rewrite the tests using other [testing styles](https://kotest.io/docs/framework/testing-styles.html) provided by Kotest.