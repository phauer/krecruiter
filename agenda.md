- Motivation: Hab eine app geschrieben, die fehlerlos ist... oder vlt doch fehler enthält? Wer weiß das schon? gibt ja keine tests und damit keine beweiß für die korrekte funktionweise!
- Vorbereitung dokumentieren: Docker, Docker-Compose, App starten und aus endpoint "/applications" muss was rauskommen

- Focus on writing clean tests! 

# ApplicationControllerTest
   
- Lifecycle
- Mock handling
- Helper methods (executing the action, entity creation with default methods, low-level values like Instants, default args)
- Clock
- General principles:
    - parameter for whats necessary
    - leave parameter out that are not necessary
    - don't assert everything again and again

# ApplicationControllerITest

- Integration test with real database. 
    - Fast and Spring-free.
    - `-noverify -XX:TieredStopAtLevel=1`
    - use `val` 
- TestContainer reuse and shortcut during dev
- Parameterized tests