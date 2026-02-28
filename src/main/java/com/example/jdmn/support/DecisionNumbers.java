package com.example.jdmn.support;

import java.math.BigDecimal;

public final class DecisionNumbers {
    private DecisionNumbers() {
    }

    public static String decimal(double value) {
        return BigDecimal.valueOf(value).toPlainString();
    }

    public static BigDecimal toBigDecimal(Number value) {
        if (value == null) {
            throw new IllegalStateException("Decision result was null");
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }
}
