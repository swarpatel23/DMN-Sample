package com.example.jdmn.examples.concepts;

import com.example.jdmn.generated.authorityrequirementmodel.KycAction;
import com.example.jdmn.generated.bkminvocationmodel.MonthlyPaymentFromBkm;
import com.example.jdmn.generated.contextdiscountmodel.ContextDiscount;
import com.example.jdmn.generated.drgchaineddecisionmodel.OfferedRate;
import com.example.jdmn.generated.listiterationmodel.WeightedTotal;
import com.example.jdmn.generated.literalexpressionmodel.LiteralRate;
import com.example.jdmn.generated.relationcatalogmodel.ShippingOptionCount;
import com.example.jdmn.generated.typesystemmodel.TypedRateWithMargin;
import com.example.jdmn.runtime.JdmnExecutionContextFactory;
import com.example.jdmn.support.DecisionNumbers;

import java.math.BigDecimal;
import java.util.Map;

public final class DmnConceptExamples {
    private final LiteralRate literalRate = new LiteralRate();
    private final ContextDiscount contextDiscount = new ContextDiscount();
    private final MonthlyPaymentFromBkm monthlyPaymentFromBkm = new MonthlyPaymentFromBkm();
    private final OfferedRate offeredRate = new OfferedRate();
    private final ShippingOptionCount shippingOptionCount = new ShippingOptionCount();
    private final WeightedTotal weightedTotal = new WeightedTotal();
    private final TypedRateWithMargin typedRateWithMargin = new TypedRateWithMargin();
    private final KycAction kycAction = new KycAction();

    public BigDecimal literalRate(double baseRate, double riskScore) {
        Number result = literalRate.applyMap(
                Map.of(
                        "baseRate", DecisionNumbers.decimal(baseRate),
                        "riskScore", DecisionNumbers.decimal(riskScore)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }

    public BigDecimal contextDiscount(double orderAmount, boolean isVip) {
        Number result = contextDiscount.applyMap(
                Map.of(
                        "orderAmount", DecisionNumbers.decimal(orderAmount),
                        "isVip", Boolean.toString(isVip)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }

    public BigDecimal monthlyPaymentFromBkm(double principal, double annualRate, int termMonths) {
        Number result = monthlyPaymentFromBkm.applyMap(
                Map.of(
                        "principal", DecisionNumbers.decimal(principal),
                        "annualRate", DecisionNumbers.decimal(annualRate),
                        "termMonths", Integer.toString(termMonths)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }

    public BigDecimal offeredRateFromDrg(int creditScore, double baseRate) {
        Number result = offeredRate.applyMap(
                Map.of(
                        "creditScore", Integer.toString(creditScore),
                        "baseRate", DecisionNumbers.decimal(baseRate)
                ),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }

    public BigDecimal relationShippingOptionCount() {
        Number result = shippingOptionCount.apply(JdmnExecutionContextFactory.standardContext());
        return DecisionNumbers.toBigDecimal(result);
    }

    public BigDecimal weightedTotalFromListIteration(double multiplier) {
        Number result = weightedTotal.applyMap(
                Map.of("multiplier", DecisionNumbers.decimal(multiplier)),
                JdmnExecutionContextFactory.standardContext()
        );
        return DecisionNumbers.toBigDecimal(result);
    }

    public BigDecimal typedRateWithMargin() {
        Number result = typedRateWithMargin.apply(JdmnExecutionContextFactory.standardContext());
        return DecisionNumbers.toBigDecimal(result);
    }

    public String kycAction(int age) {
        return kycAction.applyMap(
                Map.of("age", Integer.toString(age)),
                JdmnExecutionContextFactory.standardContext()
        );
    }
}
