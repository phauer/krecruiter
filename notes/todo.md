# TODOs

## simplification

- change db schema to have only a single table left (joining the data from both tables in one). having two tables (applicant and applications) doesn't provide any benefit. it complicates the setup without any benefit for learning how to test.
- rename "applicant" to "candidate" to remove the similarity of this word to "application".

## old

- more tests?:
  - check out java testing guide: what else can we test that require special assertions.
  - add a Scheduler - maybe it will email

## low prio

- idea: insert some mistakes in the production code. attendees should find errors by writing tests.
- use term "component test" instead of "integration test"? at least name it once in the slides.
