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
    - Pass a 200 response to the caller with a success object
    - Return an error object if the validation service returns a 500.
- `ApplicationDAO`
    - Filtering by `ApplicationState` should only return the applications with the requested state
    - Return all applications if no state is requested.
    - Order applications by `dateCreated`
    
## Hands-On Part 2: Integration Tests

- Migrate all tests from part 1 to the integration test `ApplicationControllerITest`. For this, wire the real objects together (not mocks) and test all layers at once (Controller, DAO, Client). Only migrate those tests that describe behavior of the service that is visible outside of it (ingoing requests, changed databases entries, outgoing responses). Internals (like internal data structures, exceptions, outcome objects) are implementation details and not relevant. However, the resulting behavior of those internals are relevant.
- Don't create an application and return a 400 if an required JSON field is missing. Test this with all fields (because all fields are required)
- Don't create an application and return a 400 if an invalid JSON is passed. Try at least the strings "", "asdf", "2", "{}", "[]".

## Hands-On Part 3: KotlinTest, Table-Driven Testing, Property-Based Testing

- Rewrite the `ApplicationControllerITest` of part 2 to [KotlinTest](https://github.com/kotlintest/kotlintest). The following features are useful for this task:
    - [Supported testing styles](https://github.com/kotlintest/kotlintest/blob/master/doc/styles.md)
    - [Available matchers](https://github.com/kotlintest/kotlintest/blob/master/doc/matchers.md)
    - [Inspectors for testing collections](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#inspectors)
    - [Table-driven testing for parameterized tests](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#table-driven-testing)
    - [Property-based testing to generate randomized data](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#property-based-testing-)
    - [Custom Matcher](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#custom-matchers)
    - [Soft Assertions](https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md#soft-assertions) 