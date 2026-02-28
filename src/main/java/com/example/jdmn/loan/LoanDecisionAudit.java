package com.example.jdmn.loan;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record LoanDecisionAudit(
        LoanDecision decision,
        String decisionName,
        String hitPolicy,
        Map<String, Object> inputs,
        Object output,
        List<RuleEvaluation> evaluatedRules,
        List<RuleEvaluation> matchedRules
) {
    public LoanDecisionAudit {
        Objects.requireNonNull(decision, "decision must not be null");
        Objects.requireNonNull(decisionName, "decisionName must not be null");
        Objects.requireNonNull(hitPolicy, "hitPolicy must not be null");
        Objects.requireNonNull(inputs, "inputs must not be null");
        Objects.requireNonNull(evaluatedRules, "evaluatedRules must not be null");
        Objects.requireNonNull(matchedRules, "matchedRules must not be null");
        inputs = Map.copyOf(inputs);
        evaluatedRules = List.copyOf(evaluatedRules);
        matchedRules = List.copyOf(matchedRules);
    }

    public record RuleEvaluation(
            int ruleIndex,
            String annotation,
            boolean matched,
            List<ColumnCheck> columnChecks
    ) {
        public RuleEvaluation {
            Objects.requireNonNull(annotation, "annotation must not be null");
            Objects.requireNonNull(columnChecks, "columnChecks must not be null");
            columnChecks = List.copyOf(columnChecks);
        }
    }

    public record ColumnCheck(
            int columnIndex,
            Object result,
            Boolean satisfied
    ) {
    }
}
