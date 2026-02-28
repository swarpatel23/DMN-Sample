package com.example.jdmn.testutil;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BigDecimalAssertions {
    private BigDecimalAssertions() {
    }

    public static void assertDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
