package com.example.jdmn.loan;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public record LoanApplication(
        BigDecimal applicantAge,
        BigDecimal creditScore,
        BigDecimal annualIncome,
        BigDecimal debtToIncome,
        BigDecimal requestedAmount
) {
    public LoanApplication {
        Objects.requireNonNull(applicantAge, "applicantAge must not be null");
        Objects.requireNonNull(creditScore, "creditScore must not be null");
        Objects.requireNonNull(annualIncome, "annualIncome must not be null");
        Objects.requireNonNull(debtToIncome, "debtToIncome must not be null");
        Objects.requireNonNull(requestedAmount, "requestedAmount must not be null");
    }

    public static LoanApplication of(
            int applicantAge,
            int creditScore,
            double annualIncome,
            double debtToIncome,
            double requestedAmount
    ) {
        return new LoanApplication(
                BigDecimal.valueOf(applicantAge),
                BigDecimal.valueOf(creditScore),
                BigDecimal.valueOf(annualIncome),
                BigDecimal.valueOf(debtToIncome),
                BigDecimal.valueOf(requestedAmount)
        );
    }

    public Map<String, Object> toDecisionInputs() {
        return Map.of(
                "applicantAge", applicantAge,
                "creditScore", creditScore,
                "annualIncome", annualIncome,
                "debtToIncome", debtToIncome,
                "requestedAmount", requestedAmount
        );
    }
}
