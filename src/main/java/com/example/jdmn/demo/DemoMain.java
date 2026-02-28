package com.example.jdmn.demo;

import com.example.jdmn.loan.JdmnLoanApprovalEngine;
import com.example.jdmn.loan.LoanApplication;
import com.example.jdmn.loan.LoanDecision;

import java.util.List;

public final class DemoMain {
    private DemoMain() {
    }

    public static void main(String[] args) {
        JdmnLoanApprovalEngine engine = new JdmnLoanApprovalEngine();
        List<LoanApplication> samples = List.of(
                LoanApplication.of(32, 755, 150_000, 0.24, 320_000),
                LoanApplication.of(27, 675, 72_000, 0.37, 180_000),
                LoanApplication.of(19, 740, 98_000, 0.18, 150_000),
                LoanApplication.of(45, 615, 85_000, 0.44, 300_000)
        );

        for (LoanApplication sample : samples) {
            LoanDecision decision = engine.evaluate(sample);
            System.out.println(sample + " => " + decision);
        }
    }
}
