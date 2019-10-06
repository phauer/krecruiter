# ApplicationControllerTest
   
- Lifecycle
- Mock handling
- Helper methods (executing the action, entity creation with default methods, low-level values like Instants)
- Clock

# ApplicationControllerITest

- Integration test with real database. 
    - Fast and Spring-free.
    - `-noverify -XX:TieredStopAtLevel=1`
    - use `val` 
- TestContainer reuse and shortcut during dev
- Parameterized tests