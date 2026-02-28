package com.example.jdmn.demo;

import com.example.jdmn.examples.concepts.DmnConceptExamples;

public final class DmnConceptDemoMain {
    private DmnConceptDemoMain() {
    }

    public static void main(String[] args) {
        DmnConceptExamples examples = new DmnConceptExamples();

        System.out.println("Literal expression => " + examples.literalRate(4.0, 2.5));
        System.out.println("Context discount (550, vip=true) => " + examples.contextDiscount(550, true));
        System.out.println("BKM monthly payment => " + examples.monthlyPaymentFromBkm(12_000, 0.12, 12));
        System.out.println("DRG offered rate (credit=680, base=5.0) => " + examples.offeredRateFromDrg(680, 5.0));
        System.out.println("Relation shipping option count => " + examples.relationShippingOptionCount());
        System.out.println("List iteration weighted total => " + examples.weightedTotalFromListIteration(2));
        System.out.println("Typed rate with margin => " + examples.typedRateWithMargin());
        System.out.println("Authority requirement action (age=18) => " + examples.kycAction(18));
    }
}
