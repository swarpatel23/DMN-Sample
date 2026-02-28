package com.example.jdmn.demo;

import com.example.jdmn.loan.JdmnLoanApprovalEngine;
import com.example.jdmn.loan.LoanApplication;
import com.example.jdmn.loan.LoanDecisionAudit;

public final class LoanAuditDemoMain {
    private LoanAuditDemoMain() {
    }

    public static void main(String[] args) {
        JdmnLoanApprovalEngine engine = new JdmnLoanApprovalEngine();
        LoanDecisionAudit audit = engine.evaluateWithAudit(
                LoanApplication.of(30, 640, 62_000, 0.34, 180_000)
        );

        System.out.println("Decision: " + audit.decision());
        System.out.println("Decision name: " + audit.decisionName());
        System.out.println("Hit policy: " + audit.hitPolicy());
        System.out.println("Matched rules:");
        for (LoanDecisionAudit.RuleEvaluation rule : audit.matchedRules()) {
            System.out.println("  - Rule " + rule.ruleIndex() + ": " + rule.annotation());
        }
        System.out.println("First evaluated rule checks:");
        LoanDecisionAudit.RuleEvaluation firstRule = audit.evaluatedRules().get(0);
        for (LoanDecisionAudit.ColumnCheck check : firstRule.columnChecks()) {
            System.out.println("  - Column " + check.columnIndex() + " => " + check.result());
        }
    }
}
