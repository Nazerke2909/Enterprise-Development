# Product

This project is a simple web-based system for managing homework assignments.
It allows teachers and students to follow the progress of an assignment from
the moment it is assigned until it is finally approved.

# Core item

The main entity of this project is an **Assignment**.
Each assignment has a unique identifier and a status that shows its current
stage in the homework process.

# Status table

| From | To | Allowed? | Business reason |
|------|----|----------|-----------------|
| ASSIGNED | SUBMITTED | Yes | The student submits the assigned homework. |
| SUBMITTED | CHECKED | Yes | The teacher checks the submitted homework. |
| CHECKED | ASSIGNED | No | A checked assignment cannot return directly to the initial state. |
| APPROVED | SUBMITTED | No | An approved assignment is already completed and cannot be submitted again. |

# Forbidden transitions

## CHECKED → ASSIGNED

This transition is not allowed because the assignment has already been checked.
Sending it back to the **ASSIGNED** state would break the normal order of the
homework process.

## APPROVED → SUBMITTED

This transition is also not allowed because an approved assignment is already
finished. Once it is approved, it cannot be submitted again.

# Rules

The transition logic lives in small `Rule` implementations (plain Java, no Spring).
`RuleChain` runs them in order and stops at the first failure.

| Rule | Enforces | Status table rows |
|------|----------|-------------------|
| ApprovedIsFinalRule | Stop-factor: an approved assignment is finished | APPROVED → SUBMITTED (No) |
| TransitionRule | Only the listed transitions are allowed | ASSIGNED → SUBMITTED, SUBMITTED → CHECKED, CHECKED → APPROVED (Yes); CHECKED → ASSIGNED (No) |

#Architecture
```mermaid
flowchart TD
    dto["<b>dto</b><br/>(JSON later)"]
    client["<b>client</b><br/>(HTTP later)"]
    handler["<b>handler</b><br/>(HTTP week 9)"]

    subgraph config["config"]
        direction TB
        app["Application"]
        service["AssignmentService<br/>(@Service)"]
    end

    subgraph domain["domain (no Spring)"]
        direction TB
        id["AssignmentId"]
        status["AssignmentStatus"]
        policy["AssignmentPolicy"]
        chain["RuleChain"]
        rule["Rule"]
        transition["TransitionRule"]
        approved["ApprovedIsFinalRule"]

        chain -->|runs| rule
        transition -.->|implements| rule
        approved -.->|implements| rule
    end

    dto --> domain
    client --> domain
    handler --> domain
    app --> service
    service -->|injects| rule
```
Arrows point inward, and `domain` never imports `org.springframework`.

# Database (Lab 3)

PostgreSQL only. Classroom URL: `jdbc:postgresql://localhost:5233/css`, user `css`, password `css`.
The table is written by hand in `project/src/main/resources/db/schema.sql`. Nothing generates it.

| Column | Meaning |
|--------|---------|
| `id` | surrogate key, `AssignmentId.newId()` |
| `business_key` | human number such as `HW-1042`, `UNIQUE NOT NULL` |
| `status` | text, `CHECK` in `ASSIGNED, SUBMITTED, CHECKED, APPROVED` |
| `title` | text, `NOT NULL` |

Packages: `domain` (no JDBC, no Spring), `persistence` (`AssignmentJdbc`, `JdbcTemplate` with `?`),
`config` (`AssignmentService` with `@Transactional`).
A duplicate `business_key` becomes `DuplicateAssignment` (unchecked, in `domain`).

## Terminal menu

After starting PostgreSQL and creating the table, run the application with its
interactive English-language menu:

```powershell
cd ".\project"
mvn spring-boot:run "-Dspring-boot.run.arguments=--app.cli.enabled=true"
```

The menu allows you to:

1. create an assignment;
2. show all assignments;
3. find an assignment by key;
4. change the status while checking transition rules;
0. exit.

Creating an assignment and changing its status are saved in the `assignment` table.
The status values are `ASSIGNED`, `SUBMITTED`, `CHECKED`, and `APPROVED`.