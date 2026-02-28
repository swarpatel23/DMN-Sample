package com.example.jdmn.loan;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private void assumeGeneratedDecisionIsAvailable() {
        Assumptions.assumeTrue(
                engine.isGeneratedDecisionAvailable(),
                "Generated jDMN classes are missing. Run `mvn clean test`."
        );
    }
}
