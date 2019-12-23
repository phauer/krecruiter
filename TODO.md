# TODOs

- use flyway instead of DIY-schema creation
- ApplicationController test as a mock-based test! at least the GET part.
- more tests?:
    - **check out java testing guide: what else can we test that require special assertions.**
    - POST resource: location header
    - add a Scheduler - maybe it will email
    - more complexity required? rename applicant to person and introduce recruiter and hiringManager as parts of the application
- give KotlinTest a second chance? or at least show as an alternative approach.
    - https://github.com/kotlintest/kotlintest
    - https://github.com/kotlintest/kotlintest/blob/master/doc/reference.md
    - esp. property-based testing und table-driven support ist nice.
    - auch parameterized test sehen besser aus.
    - frage ist: wie mächtig sind die matcher?
    - auch nett: soft-assertions und custom matchers
