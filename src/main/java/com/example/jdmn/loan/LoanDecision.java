package com.example.jdmn.loan;

import java.util.Locale;
import java.util.Map;

public enum LoanDecision {
    APPROVE,
    REVIEW,
    REJECT;

    public static LoanDecision fromRawResult(Object rawResult) {
        if (rawResult == null) {
            throw new IllegalStateException("Decision result was null");
        }

        if (rawResult instanceof LoanDecision decision) {
            return decision;
        }

        if (rawResult instanceof Map<?, ?> resultMap) {
            Object decision = resultMap.get("decision");
            if (decision == null && !resultMap.isEmpty()) {
                decision = resultMap.values().iterator().next();
            }
            return fromRawResult(decision);
        }

        String normalized = rawResult.toString().trim().replace("\"", "");
        return LoanDecision.valueOf(normalized.toUpperCase(Locale.ROOT));
    }
}
