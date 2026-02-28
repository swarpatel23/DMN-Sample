package com.example.jdmn.demo;

import com.example.jdmn.examples.hitpolicy.HitPolicyExamples;

public final class HitPolicyDemoMain {
    private HitPolicyDemoMain() {
    }

    public static void main(String[] args) {
        HitPolicyExamples examples = new HitPolicyExamples();

        System.out.println("UNIQUE (shipping fee)");
        System.out.println("orderAmount=50, prime=false => " + examples.shippingFee(50, false));
        System.out.println("orderAmount=50, prime=true => " + examples.shippingFee(50, true));
        System.out.println("orderAmount=150, prime=false => " + examples.shippingFee(150, false));

        System.out.println("ANY (transaction action)");
        System.out.println(
                "amount=15000, countryRisk=HIGH, failedOtpAttempts=4 => "
                        + examples.transactionAction(15000, "HIGH", 4)
        );
        System.out.println(
                "amount=2000, countryRisk=LOW, failedOtpAttempts=0 => "
                        + examples.transactionAction(2000, "LOW", 0)
        );

        System.out.println("COLLECT + SUM (insurance surcharge)");
        System.out.println(
                "age=67, smoker=true, preExisting=false, bmi=31 => "
                        + examples.insuranceSurchargePercent(67, true, false, 31)
        );
        System.out.println(
                "age=35, smoker=false, preExisting=true, bmi=24 => "
                        + examples.insuranceSurchargePercent(35, false, true, 24)
        );
    }
}
