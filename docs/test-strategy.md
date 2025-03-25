# Test strategy

The platform is small but exercises three different test layers — all
of them fall out naturally from the BPMN model, so they're documented
here in the same shape a tester would use.

## Layers

| Layer        | Tooling              | Examples                                       |
|--------------|----------------------|------------------------------------------------|
| Unit         | JUnit 5 + Mockito    | `RequestValidatorTest`, `LegacyXmlBridgeTest`  |
| Process IT   | Spring Boot + Camunda assert | `ApprovalProcessIT`                    |
| Smoke (CI)   | Maven verify on every push   | exercises both layers                  |

## Test-case IDs

Every assertion that maps to a known business rule has an ID in the
form `TC-<area>-<n>`. Production code throws with the same ID embedded
in the message so a failing run gives the on-call a direct pointer to
the test case that documents the rule.

| ID            | Area     | Rule                                                  |
|---------------|----------|-------------------------------------------------------|
| TC-VAL-001    | Validate | request must include an amount                        |
| TC-VAL-002    | Validate | request must include a request id                     |
| TC-XSLT-001   | Legacy   | partner XML must transform to canonical JSON shape    |
| TC-PROC-200   | Process  | amount ≤ 5000 auto-approves and ends                  |
| TC-PROC-201   | Process  | amount > 5000 waits at "Manager review"               |

## How a failing test reads

The intent is that a failing CI run is a self-describing defect:

```
TC-PROC-201 expected the process to wait at Task_ManagerReview
but it had already passed it. Likely cause: amount-gate predicate
flipped or the XSLT bridge dropped the amount field.
```

Adding a new business rule means
1. write its `TC-…` id in this file,
2. add the assertion in the test,
3. make production code throw / emit using the same id.
