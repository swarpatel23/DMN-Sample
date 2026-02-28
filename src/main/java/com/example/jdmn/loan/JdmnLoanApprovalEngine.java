package com.example.jdmn.loan;

import com.example.jdmn.generated.loanapprovalmodel.LoanApproval;
import com.example.jdmn.runtime.JdmnExecutionContextFactory;
import com.gs.dmn.runtime.listener.TreeTraceEventListener;
import com.gs.dmn.runtime.listener.node.ColumnNode;
import com.gs.dmn.runtime.listener.node.DRGElementNode;
import com.gs.dmn.runtime.listener.node.RuleNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class JdmnLoanApprovalEngine {
    private final LoanApproval loanApproval = new LoanApproval();

    public LoanDecision evaluate(LoanApplication loanApplication) {
        Objects.requireNonNull(loanApplication, "loanApplication must not be null");
        String rawResult = loanApproval.apply(
                loanApplication.annualIncome(),
                loanApplication.applicantAge(),
                loanApplication.creditScore(),
                loanApplication.debtToIncome(),
                loanApplication.requestedAmount(),
                JdmnExecutionContextFactory.standardContext()
        );
        return LoanDecision.fromRawResult(rawResult);
    }

    public LoanDecisionAudit evaluateWithAudit(LoanApplication loanApplication) {
        Objects.requireNonNull(loanApplication, "loanApplication must not be null");

        TreeTraceEventListener trace = new TreeTraceEventListener();
        String rawResult = loanApproval.apply(
                loanApplication.annualIncome(),
                loanApplication.applicantAge(),
                loanApplication.creditScore(),
                loanApplication.debtToIncome(),
                loanApplication.requestedAmount(),
                JdmnExecutionContextFactory.contextWithEventListener(trace)
        );
        LoanDecision decision = LoanDecision.fromRawResult(rawResult);

        DRGElementNode node = trace.getRoot();
        if (node == null || node.getElement() == null) {
            throw new IllegalStateException("Trace did not capture decision metadata");
        }

        List<LoanDecisionAudit.RuleEvaluation> evaluatedRules = node.getRuleNodes().stream()
                .map(JdmnLoanApprovalEngine::toRuleEvaluation)
                .toList();
        List<LoanDecisionAudit.RuleEvaluation> matchedRules = evaluatedRules.stream()
                .filter(LoanDecisionAudit.RuleEvaluation::matched)
                .toList();

        return new LoanDecisionAudit(
                decision,
                node.getElement().getName(),
                node.getElement().getHitPolicy().name(),
                Map.copyOf(loanApplication.toDecisionInputs()),
                node.getOutput(),
                evaluatedRules,
                matchedRules
        );
    }

    private static LoanDecisionAudit.RuleEvaluation toRuleEvaluation(RuleNode ruleNode) {
        List<LoanDecisionAudit.ColumnCheck> checks = ruleNode.getColumnNodes().stream()
                .map(JdmnLoanApprovalEngine::toColumnCheck)
                .toList();
        return new LoanDecisionAudit.RuleEvaluation(
                ruleNode.getRule().getIndex(),
                ruleNode.getRule().getAnnotation(),
                ruleNode.isMatched(),
                checks
        );
    }

    private static LoanDecisionAudit.ColumnCheck toColumnCheck(ColumnNode columnNode) {
        Object result = columnNode.getResult();
        Boolean satisfied = result instanceof Boolean value ? value : null;
        return new LoanDecisionAudit.ColumnCheck(columnNode.getColumnIndex(), result, satisfied);
    }

    public boolean isGeneratedDecisionAvailable() {
        return true;
    }
}
