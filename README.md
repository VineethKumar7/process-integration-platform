# Process Integration Platform

A small Spring Boot service that automates enterprise approval workflows
using BPMN (Camunda 7), with an XSLT bridge for legacy partner XML formats
and a thin JavaScript front-end for kicking off approvals.

> Built to replace a manual hand-off workflow with a deterministic,
> testable pipeline — and to give myself a sandbox for practicing
> process automation, BPMN, and end-to-end testing.

## What it does

1. A user (or upstream partner system) submits a request — either through
   the JS UI or by POSTing XML to the REST endpoint.
2. The XSLT layer normalises legacy partner XML into the canonical request
   schema.
3. Camunda starts an `ApprovalProcess` BPMN instance with the request as
   process variables.
4. The BPMN flow walks reviewer service tasks, branches on policy rules,
   and persists outcome events.
5. A final webhook task notifies upstream systems and writes audit data.

## Stack

- Java 17, Spring Boot 3
- Camunda 7 (embedded engine)
- BPMN 2.0 + Camunda Modeler diagrams
- XML / XSLT (`net.sf.saxon`) for legacy bridge
- Vanilla JavaScript for the small operator UI
- JUnit 5 + Camunda assert for process integration tests
- Maven + GitHub Actions for CI

## Why I built this

The Werkstudent role I was preparing for emphasised software testing,
test-case design and BPMN process workflows. I wanted a project where I
could practise:

- writing test cases against process flows (not just unit logic),
- structuring failure descriptions so a reviewer knows exactly which
  gateway/decision/branch failed,
- handling messy partner input via XSLT before it hits the engine.

## Running locally

```bash
mvn spring-boot:run
# UI at http://localhost:8080
# Camunda Cockpit at http://localhost:8080/camunda  (admin / admin)
```

## Tests

```bash
mvn test          # unit tests
mvn verify        # process integration tests
```

See `docs/test-strategy.md` for a write-up of how each BPMN path is
covered.
