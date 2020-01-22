CREATE TABLE IF NOT EXISTS applicant
(
    id          SERIAL PRIMARY KEY,
    firstName   VARCHAR(60),
    lastName    VARCHAR(60),
    street      VARCHAR(60),
    city        VARCHAR(30),
    dateCreated TIMESTAMP
);

CREATE TABLE IF NOT EXISTS application
(
    id          SERIAL PRIMARY KEY,
    applicantId INTEGER REFERENCES applicant (id),
    jobTitle    VARCHAR(120),
    state       VARCHAR(50),
    dateCreated TIMESTAMP
)