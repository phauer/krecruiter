# Tasks

## Hands-On Part 1: Mock-Based Unit Tests

- `ApplicationController`
    - List applications (`GET /applications`)
        - A `GET` request on `/applications` should return a list of JSON documents with the fields `id`, `fullName`, `jobTitle`, `state` and `dateCreated` from the database. 
    - Create an application (`POST /applications`)
        - A `POST` request on `/applications` (containing an application as JSON in the body) creates an application and an applicant entry in the database with the posted values and the current timestamp.
        - Reject application creation when the AddressValidationService says that the submitted address is invalid.
        - Return a 500 status code if the request to the AddressValidationService was not successfully
- `AddressValidationClient`
    - Pass a 200 response to the caller with a success object.
    - Return an error object if the validation service returns a 500.
- `ApplicationDAO`
    - Filtering by `ApplicationState` should only return the applications with the requested state.
    - Return all applications if no state is requested.
    - Order applications by `dateCreated`.
    
## Hands-On Part 2: Integration Tests

- Migrate all tests from part 1 to the integration test `ApplicationControllerITest`. For this, wire the real objects together (not mocks) and test all layers at once (Controller, DAO, Client). Only migrate those tests that describe behavior of the service that is visible outside of it (ingoing requests, changed databases entries, outgoing responses). Internals (like internal data structures, exceptions, outcome objects) are implementation details and not relevant. Only the resulting behavior of those internals are relevant.
- Don't create an application and return a 400 if an required JSON field is missing. Test this with all fields (because all fields are required).
- Don't create an application and return a 400 if an invalid JSON is passed. Try at least the strings "", "asdf", "2", "{}", "[]".

## Hands-On Part 3: KotlinTest, Table-Driven Testing, Property-Based Testing

- Migrate all test from part 2 to the test `ApplicationControllerKotlinTest` which bases on [KotlinTest](https://github.com/kotlintest/kotlintest). Mainly, this requires changing the matchers and the definition of test names, nesting and parameterized test.
    - The `FreeSpec` [testing style](https://github.com/kotlintest/kotlintest/blob/master/doc/styles.md) supports grouping which can be used instead of JUnit5's `@Nested`.
    - Use KotlinTest's [matchers](https://github.com/kotlintest/kotlintest/blob/master/doc/matchers.md) instead of AssertJ. The matchers start with `should*`. This can be used for auto-completion.
    - Use [table-driven testing for parameterized tests](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#table-driven-testing)
- Write a [property-based test](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#property-based-testing-) that generates random values for the application fields and create an application with this data.
    - You can rewrite an existing test for this.
    - You can write a [custom generator](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#custom-generators) to create an `ApplicationCreationDTO` with randomized values.
    - Hint: Unfortunately, property-based tests and table-driven tests don't call the defined `beforeTest()` method. So you have to call it manually with `beforeTest(mockk<TestCase>())` before each iteration.  
- Other interesting features of KotlinTest:
    - [Inspectors for testing collections](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#inspectors)
    - [Custom Matcher](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#custom-matchers)
    
## Optional Tasks

- Test that a correct `location` header is returned in the response for creating an application. It should look like `/applications/<applicationId>`.
- Rewrite the tests using other [testing styles](https://github.com/kotlintest/kotlintest/blob/master/doc/styles.md) provided by KotlinTest.