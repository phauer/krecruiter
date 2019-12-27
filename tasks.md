# Tasks

## Hands-On Part 1: Mock-Based Unit Tests

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

- Migrate all tests from part 1 to integration tests. For this, wire the real objects together (not mocks) and test all layers at once (Controller, DAO, Client). Only migrate those tests that describe behavior of the service that is visible outside of it (ingoing requests, changed databases entries, outgoing responses). Internals (like internal data structures, exceptions, outcome objects) are implementation details and not relevant. However, the resulting behavior of those internals are relevant.
- Don't create an application and return a 400 if an required JSON field is missing. Test this with all fields (because all fields are required)
- Don't create an application and return a 400 if an invalid JSON is passed. Try at least the strings "", "asdf", "2", "{}", "[]".
