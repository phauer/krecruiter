# Tasks

## Hands-On Part 1: Mock-Based Unit Tests

- `ApplicationDAO`
    - Tip: With `PostgreSQLInstance.jdbi` you can create a `Jdbi` instance which can be passed to the `ApplicationDAO`. The `TestDAO` can be used to useful functions to create the schema and fill the table with test data.
    - Filtering by `ApplicationState` should only return the applications with the requested state.
    - Return all applications if no state is requested.
    - Order applications by `dateCreated`.
- `AddressValidationClient`
    - Tip: Check out the file `util/MockServerUtils.kt` in the test folder. It contains many useful functions to use the WebMockServer API.
    - Pass a 200 response to the caller with a success object.
    - Return an error object if the validation service returns a 500.
- `ApplicationController`
    - Tip: Check out the file `util/MockMvcUtils.kt` in the test folder. It contains many useful functions to use Spring's MockMvc API.
    - List applications (`GET /applications`)
        - A `GET` request on `/applications` should return a list of JSON documents with the fields `id`, `fullName`, `jobTitle`, `state` and `dateCreated` from the database. 
    - Create an application (`POST /applications`)
        - A `POST` request on `/applications` (containing an application as JSON in the body) creates an application and an applicant entry in the database with the posted values and the current timestamp.
        - Reject application creation when the AddressValidationService says that the submitted address is invalid.
        - Return a 500 status code if the request to the AddressValidationService was not successfully

    
## Hands-On Part 2: Integration Tests

- Migrate all tests from part 1 to the integration test `ApplicationControllerITest`. For this, wire the real objects together (not mocks) and test all layers at once (Controller, DAO, Client). Only migrate those tests that describe behavior of the service that is visible outside of it (ingoing requests, changed databases entries, outgoing responses). Internals (like internal data structures, exceptions, outcome objects) are implementation details and not relevant. Only the resulting behavior of those internals are relevant.
- Don't create an application and return a 400 if an required JSON field is missing. Test this with all fields (because all fields are required).
- Don't create an application and return a 400 if an invalid JSON is passed. Try at least the strings "", "asdf", "2", "{}", "[]".


## Hands-On Part 3: Kotest, Table-Driven Testing, Property-Based Testing

- Migrate all test from part 2 to the test `ApplicationControllerKotest` which bases on [Kotest](https://github.com/kotest/kotest/). Mainly, this requires changing the matchers and the definition of test names, nesting and parameterized test.
    - The `FreeSpec` [testing style](https://github.com/kotest/kotest/blob/master/doc/styles.md) supports grouping which can be used instead of JUnit5's `@Nested`.
    - Use Kotest's [matchers](https://github.com/kotest/kotest/blob/master/doc/matchers.md) instead of AssertJ. The matchers start with `should*`. This can be used for auto-completion.
    - Use [data-driven testing for parameterized tests](https://github.com/kotest/kotest/blob/master/doc/data_driven_testing.md)
- Write a [property-based test](https://github.com/kotest/kotest/blob/master/doc/property_testing.md) that generates random values for the application fields and create an application with this data.
    - You can rewrite an existing test for this.
    - Hint: Unfortunately, property-based tests and table-driven tests don't call the defined `beforeTest()` method. So you have to call it manually with `beforeTest(mockk<TestCase>())` before each iteration.  
- Other interesting features of Kotest:
    - [Inspectors for testing collections](https://github.com/kotest/kotest/blob/master/doc/reference.md#inspectors)
    - [Custom Matcher](https://github.com/kotest/kotest/blob/master/doc/reference.md#custom-matchers)
    
## Optional Tasks

- Make your test code even cleaner, expressive and understandable.
    - Read briefly through [Modern Best Practices for Testing in Java](https://phauer.com/2019/modern-best-practices-testing-java/). Although the examples are written in Java, the described best practices also apply to Kotlin test code. 
    - Are there any recommendations that can improve your test code? Try to refactor your code.
- Test that a correct `location` header is returned in the response for creating an application. It should look like `/applications/<applicationId>`.
- Rewrite the tests using other [testing styles](https://github.com/kotest/kotest/blob/master/doc/styles.md) provided by Kotest.