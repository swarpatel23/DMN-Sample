package com.example.jdmn.examples.hitpolicy;

import com.example.jdmn.generated.insurancesurchargecollectsummodel.InsuranceSurchargeCollectSum;
import com.example.jdmn.generated.shippingfeeuniquemodel.ShippingFeeUnique;
import com.example.jdmn.generated.transactionactionanymodel.TransactionActionAny;
import com.example.jdmn.runtime.JdmnExecutionContextFactory;
import com.example.jdmn.support.DecisionNumbers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public final class HitPolicyExamples {
    private final ShippingFeeUnique shippingFeeUnique = new ShippingFeeUnique();
    private final TransactionActionAny transactionActionAny = new TransactionActionAny();
    private final InsuranceSurchargeCollectSum insuranceSurchargeCollectSum = new InsuranceSurchargeCollectSum();

    public BigDecimal shippingFee(double orderAmount, boolean isPrimeCustomer) {
        Number result = shippingFeeUnique.applyMap(
                Map.of(
                        "orderAmount", DecisionNumbers.decimal(orderAmount),
                        "isPrimeCustomer", Boolean.toString(isPrimeCustomer)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }

    public String transactionAction(double transactionAmount, String countryRisk, int failedOtpAttempts) {
        Objects.requireNonNull(countryRisk, "countryRisk must not be null");
        return transactionActionAny.applyMap(
                Map.of(
                        "transactionAmount", DecisionNumbers.decimal(transactionAmount),
                        "countryRisk", countryRisk,
                        "failedOtpAttempts", Integer.toString(failedOtpAttempts)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
    }

    public BigDecimal insuranceSurchargePercent(
            int applicantAge,
            boolean isSmoker,
            boolean hasPreExistingCondition,
            double bmi
    ) {
        Number result = insuranceSurchargeCollectSum.applyMap(
                Map.of(
                        "applicantAge", Integer.toString(applicantAge),
                        "isSmoker", Boolean.toString(isSmoker),
                        "hasPreExistingCondition", Boolean.toString(hasPreExistingCondition),
                        "bmi", DecisionNumbers.decimal(bmi)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }
}
