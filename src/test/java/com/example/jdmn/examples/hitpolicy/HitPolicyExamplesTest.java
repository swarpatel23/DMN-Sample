package com.example.jdmn.examples.hitpolicy;

import org.junit.jupiter.api.Test;

import static com.example.jdmn.testutil.BigDecimalAssertions.assertDecimalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HitPolicyExamplesTest {
    private final HitPolicyExamples examples = new HitPolicyExamples();

    @Test
    void uniqueHitPolicyPicksSingleMatchingRow() {
        // UNIQUE: only the "non-prime below 100" row matches, so standard shipping applies.
        assertDecimalEquals("7.99", examples.shippingFee(50, false));
        // UNIQUE: "prime below 100" is the only match, so shipping is free.
        assertDecimalEquals("0", examples.shippingFee(50, true));
        // UNIQUE: "order >= 100" is the only match, so shipping is free.
        assertDecimalEquals("0", examples.shippingFee(150, false));
    }

    @Test
    void anyHitPolicyAllowsMultipleMatchesWithSameOutput() {
        // ANY: amount, risk, and OTP rules can all match, but they all return REVIEW, so result is valid.
        assertEquals("REVIEW", examples.transactionAction(15000, "HIGH", 4));
        // ANY: no review rule matches, so only the normal-flow APPROVE rule applies.
        assertEquals("APPROVE", examples.transactionAction(2000, "LOW", 0));
    }

    @Test
    void collectSumHitPolicyAggregatesAllMatchedRows() {
        // COLLECT+SUM: age(12) + smoker(15) + bmi(8) = 35.
        assertDecimalEquals("35", examples.insuranceSurchargePercent(67, true, false, 31));
        // COLLECT+SUM: only pre-existing-condition surcharge applies, so total is 10.
        assertDecimalEquals("10", examples.insuranceSurchargePercent(35, false, true, 24));
    }
}
