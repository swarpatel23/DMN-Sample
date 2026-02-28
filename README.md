# jDMN Decisioning Demo

This repository shows how to model policy decisions in DMN and execute them in Java using jDMN.

## What This Project Demonstrates

1. A realistic loan-approval decision table (`FIRST` hit policy).
2. Additional hit policies (`UNIQUE`, `ANY`, `COLLECT + SUM`).
3. Non-table DMN constructs (literal, context, BKM, DRG chaining, relation, list iteration, types, authority requirement).
4. A Maven flow where DMN models are converted to Java and validated by tests.

## Why DMN Fits This Use Case

- Loan underwriting is policy-driven logic, which maps directly to decision tables and FEEL expressions.
- Business/risk users can review decision logic in a DMN modeler without reading Java code.
- Rule changes are safer: update model -> regenerate code -> run tests.
- Behavior is auditable: hit policy and rule ordering are explicit in the model.
- DMN is a portable standard, not a proprietary rule format.

## When It Makes Sense To Use DMN

DMN is usually a strong fit when your problem looks like business policy evaluation rather than algorithm-heavy computation.

Good fit patterns:

- Policy and eligibility decisions:
  - loan approval, insurance underwriting, claims routing, KYC/AML checks, pricing tiers, discount eligibility.
- Rule logic changes often:
  - thresholds/conditions are updated by business teams and must be shipped safely.
- Explainability and auditability are required:
  - you need to answer "which rule fired and why?" for regulators, audits, or customer support.
- Multiple teams collaborate on rules:
  - analysts define policy, developers integrate execution and tests.
- Decisions are composable:
  - you benefit from DRG-style chaining (one decision feeding another) and reusable BKMs.

Usually not a great fit:

- Heavy numerical/ML workloads:
  - optimization, large-scale forecasting, vector math, model training/inference pipelines.
- Highly procedural workflows:
  - long-running orchestration with retries/timeouts/state machines (better handled by workflow engines).
- Low-value static logic:
  - simple code branches that rarely change may be clearer as plain application code.

## Quick Start

```bash
mvn clean test
```

Run demos:

```bash
mvn -q exec:java -Dexec.mainClass=com.example.jdmn.demo.DemoMain
mvn -q exec:java -Dexec.mainClass=com.example.jdmn.demo.HitPolicyDemoMain
mvn -q exec:java -Dexec.mainClass=com.example.jdmn.demo.DmnConceptDemoMain
mvn -q exec:java -Dexec.mainClass=com.example.jdmn.demo.LoanAuditDemoMain
```

## Audit And Explainability

`JdmnLoanApprovalEngine` has two evaluation modes:

- `evaluate(...)` returns only `LoanDecision`.
- `evaluateWithAudit(...)` returns `LoanDecisionAudit` with:
  - decision metadata (`decisionName`, `hitPolicy`)
  - input snapshot (`inputs`)
  - all evaluated rules (`evaluatedRules`)
  - matched rules (`matchedRules`)
  - per-column condition results (`columnChecks`)

This gives you direct answers to:

- Which rule fired?
  - `audit.matchedRules().get(0).ruleIndex()` and `.annotation()`
- Why did it fire (or not fire)?
  - `audit.evaluatedRules()` and each rule's `columnChecks` values (`true`/`false`).

## Example Catalog (What And Why)

| DMN file | Category | Why this example exists |
|---|---|---|
| `src/main/resources/dmn/loan-approval.dmn` | Decision table (`FIRST`) | Shows realistic underwriting where rule order determines the final outcome. |
| `src/main/resources/dmn/shipping-fee-unique.dmn` | Hit policy `UNIQUE` | Shows cases where exactly one rule must match to keep outputs deterministic. |
| `src/main/resources/dmn/transaction-action-any.dmn` | Hit policy `ANY` | Shows multiple matching rules are allowed only when they agree on the same output. |
| `src/main/resources/dmn/insurance-surcharge-collect-sum.dmn` | Hit policy `COLLECT + SUM` | Shows independent matched rules being aggregated into a total surcharge. |
| `src/main/resources/dmn/literal-expression.dmn` | Literal expression | Smallest DMN decision: formula-only, no table. |
| `src/main/resources/dmn/context-discount.dmn` | Context | Demonstrates named intermediate values for readable calculations. |
| `src/main/resources/dmn/bkm-invocation.dmn` | BKM + invocation | Demonstrates reusable business functions called by decisions. |
| `src/main/resources/dmn/drg-chained-decision.dmn` | DRG chaining | Demonstrates decision decomposition (one decision feeds another). |
| `src/main/resources/dmn/relation-catalog.dmn` | Relation | Demonstrates in-model tabular data consumed by another decision. |
| `src/main/resources/dmn/list-iteration.dmn` | FEEL list/for/sum | Demonstrates iteration and aggregation inside DMN expressions. |
| `src/main/resources/dmn/type-system.dmn` | Item definitions | Demonstrates explicit domain types/type aliases in DMN. |
| `src/main/resources/dmn/authority-requirement.dmn` | Governance metadata | Demonstrates ownership/authority traceability around decisions. |

## Project Layout

### Core loan decision

- `src/main/java/com/example/jdmn/loan/LoanApplication.java`
- `src/main/java/com/example/jdmn/loan/LoanDecision.java`
- `src/main/java/com/example/jdmn/loan/LoanDecisionAudit.java`
- `src/main/java/com/example/jdmn/loan/JdmnLoanApprovalEngine.java`
- `src/test/java/com/example/jdmn/loan/JdmnLoanApprovalEngineTest.java`

### Runtime/support

- `src/main/java/com/example/jdmn/runtime/JdmnExecutionContextFactory.java`
- `src/main/java/com/example/jdmn/support/DecisionNumbers.java`
- `src/main/java/com/gs/dmn/runtime/ContextImpl.java` (compatibility shim for relation code generation)
- `src/test/java/com/example/jdmn/testutil/BigDecimalAssertions.java`

### Examples

- Hit policy wrappers/tests:
  - `src/main/java/com/example/jdmn/examples/hitpolicy/HitPolicyExamples.java`
  - `src/test/java/com/example/jdmn/examples/hitpolicy/HitPolicyExamplesTest.java`
- Non-table concept wrappers/tests:
  - `src/main/java/com/example/jdmn/examples/concepts/DmnConceptExamples.java`
  - `src/test/java/com/example/jdmn/examples/concepts/DmnConceptExamplesTest.java`
- Runnable demos:
  - `src/main/java/com/example/jdmn/demo/DemoMain.java`
  - `src/main/java/com/example/jdmn/demo/HitPolicyDemoMain.java`
  - `src/main/java/com/example/jdmn/demo/DmnConceptDemoMain.java`
  - `src/main/java/com/example/jdmn/demo/LoanAuditDemoMain.java`

## Build Flow

1. DMN models under `src/main/resources/dmn` are transformed to Java by `jdmn-maven-plugin` during `generate-sources`.
2. Generated classes are added to compilation via `build-helper-maven-plugin`.
3. Tests validate expected decision behavior and document why each output is expected.

## Notes For Business Users

DMN files are XML on disk, but they are normally authored in a DMN modeler UI (decision table grid/diagram), not by hand.

Suggested collaboration loop:

1. Analysts edit in DMN modeler and export `.dmn`.
2. Developers commit updated DMN under `src/main/resources/dmn`.
3. CI/local run `mvn clean test` to validate generated code and expected outcomes.
