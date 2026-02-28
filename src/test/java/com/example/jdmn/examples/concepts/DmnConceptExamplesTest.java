package com.example.jdmn.examples.concepts;

import org.junit.jupiter.api.Test;

import static com.example.jdmn.testutil.BigDecimalAssertions.assertDecimalEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DmnConceptExamplesTest {
    private final DmnConceptExamples examples = new DmnConceptExamples();

    @Test
    void literalExpressionExample() {
        // Literal expression: baseRate + (riskScore * 0.1) = 4.0 + (2.5 * 0.1) = 4.25.
        assertDecimalEquals("4.25", examples.literalRate(4.0, 2.5));
    }

    @Test
    void contextExample() {
        // Context entries: baseDiscount(15) + vipBonus(3) = totalDiscount(18).
        assertDecimalEquals("18", examples.contextDiscount(550, true));
        // Context entries: low amount and non-VIP gives baseDiscount(0) + vipBonus(0) = 0.
        assertDecimalEquals("0", examples.contextDiscount(80, false));
    }

    @Test
    void bkmAndInvocationExample() {
        // BKM function: principal/termMonths + principal*annualRate/12 = 12000/12 + 12000*0.12/12 = 1120.
        assertDecimalEquals("1120", examples.monthlyPaymentFromBkm(12_000, 0.12, 12));
    }

    @Test
    void drgExample() {
        // DRG chain: creditScore=780 => RiskBand LOW => OfferedRate = baseRate (5.0).
        assertDecimalEquals("5", examples.offeredRateFromDrg(780, 5.0));
        // DRG chain: creditScore=680 => RiskBand MEDIUM => OfferedRate = baseRate + 1 (6.0).
        assertDecimalEquals("6", examples.offeredRateFromDrg(680, 5.0));
    }

    @Test
    void relationExample() {
        // Relation has three rows (STANDARD, EXPRESS, OVERNIGHT), so count(...) must return 3.
        assertDecimalEquals("3", examples.relationShippingOptionCount());
    }

    @Test
    void listIterationExample() {
        // List iteration: sum(for x in [10,20,30] return x*2) = 20 + 40 + 60 = 120.
        assertDecimalEquals("120", examples.weightedTotalFromListIteration(2));
    }

    @Test
    void typeSystemExample() {
        // Typed decision uses DiscountRate alias: TypedRate(12.5) + margin(2.5) = 15.0.
        assertDecimalEquals("15", examples.typedRateWithMargin());
    }

    @Test
    void knowledgeSourceAndAuthorityRequirementExample() {
        // Authority requirement adds governance metadata; decision logic still evaluates age condition.
        assertEquals("ENHANCED_DUE_DILIGENCE", examples.kycAction(18));
        assertEquals("STANDARD_DUE_DILIGENCE", examples.kycAction(30));
    }
}
