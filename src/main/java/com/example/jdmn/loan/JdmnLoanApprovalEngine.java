package com.example.jdmn.loan;

import com.example.jdmn.generated.loanapprovalmodel.LoanApproval;
import com.example.jdmn.runtime.JdmnExecutionContextFactory;

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

    public boolean isGeneratedDecisionAvailable() {
        return true;
    }
}
