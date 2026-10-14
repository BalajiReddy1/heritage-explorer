package com.example.heritage.util;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/** Currency formatting and INR conversion with fixed indicative rates. */
public final class Money {

    public enum Unit { INR, USD, EUR }

    public static final double INR_PER_USD = 88.0;
    public static final double INR_PER_EUR = 103.0;

    private static final Locale INDIA = Locale.forLanguageTag("en-IN");

    private Money() {
    }

    public static double convert(double inr, Unit unit) {
        switch (unit) {
            case USD:
                return inr / INR_PER_USD;
            case EUR:
                return inr / INR_PER_EUR;
            default:
                return inr;
        }
    }

    /** Converts a rupee amount and formats it, for example "₹1,100", "$12.50" or "€10.68". */
    public static String format(double inr, Unit unit) {
        NumberFormat nf;
        if (unit == Unit.INR) {
            nf = NumberFormat.getCurrencyInstance(INDIA);
            boolean whole = inr == Math.rint(inr);
            nf.setMinimumFractionDigits(whole ? 0 : 2);
            nf.setMaximumFractionDigits(whole ? 0 : 2);
        } else {
            nf = NumberFormat.getCurrencyInstance(Locale.US);
            nf.setCurrency(Currency.getInstance(unit.name()));
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);
        }
        return nf.format(convert(inr, unit));
    }

    public static String inr(double amount) {
        return format(amount, Unit.INR);
    }
}
