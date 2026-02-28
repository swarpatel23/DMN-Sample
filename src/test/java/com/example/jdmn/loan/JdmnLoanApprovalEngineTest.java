package com.example.jdmn.loan;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdmnLoanApprovalEngineTest {
    private final JdmnLoanApprovalEngine engine = new JdmnLoanApprovalEngine();

    @Test
    void approvesPrimeBorrower() {
        assumeGeneratedDecisionIsAvailable();

        LoanDecision decision = engine.evaluate(LoanApplication.of(35, 760, 180_000, 0.22, 450_000));
        // FIRST hit policy: this input reaches the prime-approval row before the fallback REVIEW row.
        assertEquals(LoanDecision.APPROVE, decision);
    }

    @Test
    void rejectsHighRiskBorrower() {
        assumeGeneratedDecisionIsAvailable();

        LoanDecision decision = engine.evaluate(LoanApplication.of(24, 570, 70_000, 0.39, 120_000));
        // FIRST hit policy: creditScore < 580 matches an early reject rule, so REJECT wins immediately.
        assertEquals(LoanDecision.REJECT, decision);
    }

    @Test
    void routesBorderlineCaseToManualReview() {
        assumeGeneratedDecisionIsAvailable();

        LoanDecision decision = engine.evaluate(LoanApplication.of(30, 640, 62_000, 0.34, 180_000));
        // FIRST hit policy: no reject/approve row matches, so the final catch-all rule returns REVIEW.
        assertEquals(LoanDecision.REVIEW, decision);
    }

    @Test
    void auditShowsMatchedApproveRuleAndColumnChecks() {
        assumeGeneratedDecisionIsAvailable();

        LoanDecisionAudit audit = engine.evaluateWithAudit(LoanApplication.of(35, 760, 180_000, 0.22, 450_000));

        assertEquals(LoanDecision.APPROVE, audit.decision());
        assertEquals("LoanApproval", audit.decisionName());
        assertEquals("FIRST", audit.hitPolicy());
        assertEquals("APPROVE", audit.output());
        assertEquals(1, audit.matchedRules().size());

        LoanDecisionAudit.RuleEvaluation matched = audit.matchedRules().get(0);
        // Generated rule index is 1-based; rule 4 is the "prime borrower" approval row.
        assertEquals(4, matched.ruleIndex());
        assertEquals("Prime borrowers can be approved for larger loans.", matched.annotation());
        assertTrue(matched.matched());
        assertEquals(5, matched.columnChecks().size());
        assertTrue(matched.columnChecks().stream().allMatch(check -> Boolean.TRUE.equals(check.satisfied())));
    }

    @Test
    void auditShowsWhyBorderlineCaseFallsThroughToReview() {
        assumeGeneratedDecisionIsAvailable();

        LoanDecisionAudit audit = engine.evaluateWithAudit(LoanApplication.of(30, 640, 62_000, 0.34, 180_000));

        assertEquals(LoanDecision.REVIEW, audit.decision());
        assertEquals(1, audit.matchedRules().size());
        LoanDecisionAudit.RuleEvaluation matched = audit.matchedRules().get(0);
        assertEquals(6, matched.ruleIndex());
        assertEquals("Everything else goes to manual review.", matched.annotation());

        // First rule (under 21) fails immediately on first condition, explaining why evaluation continues.
        LoanDecisionAudit.RuleEvaluation firstRule = audit.evaluatedRules().get(0);
        assertEquals(1, firstRule.ruleIndex());
        assertFalse(firstRule.matched());
        assertEquals(1, firstRule.columnChecks().size());
        assertEquals(Boolean.FALSE, firstRule.columnChecks().get(0).satisfied());

        // Audit payload keeps concrete input values for regulatory/support replay.
        assertEquals(new BigDecimal("30"), audit.inputs().get("applicantAge"));
    }

    private void assumeGeneratedDecisionIsAvailable() {
        Assumptions.assumeTrue(
                engine.isGeneratedDecisionAvailable(),
                "Generated jDMN classes are missing. Run `mvn clean test`."
        );
    }
}
