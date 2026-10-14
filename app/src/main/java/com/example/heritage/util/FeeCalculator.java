package com.example.heritage.util;

import com.example.heritage.data.Site;
import com.example.heritage.data.Trip;

/** Ticket cost arithmetic for a visit. All amounts are in rupees. */
public final class FeeCalculator {

    public static final int GUIDE_FEE = 500;
    public static final int AUDIO_FEE_PER_PERSON = 100;

    private FeeCalculator() {
    }

    public static int ticketPrice(Site site, String ticketType) {
        return Trip.TICKET_FOREIGN.equals(ticketType) ? site.feeForeign : site.feeIndian;
    }

    public static double tickets(int price, int visitors) {
        return (double) price * visitors;
    }

    public static double guide(boolean hired) {
        return hired ? GUIDE_FEE : 0;
    }

    public static double audio(boolean taken, int visitors) {
        return taken ? (double) AUDIO_FEE_PER_PERSON * visitors : 0;
    }

    public static double total(Site site, String ticketType, int visitors, boolean guide, boolean audio) {
        return tickets(ticketPrice(site, ticketType), visitors) + guide(guide) + audio(audio, visitors);
    }

    /** @throws ArithmeticException when visitors is 0, so the caller can show a message. */
    public static double perPerson(double total, int visitors) {
        if (visitors == 0) throw new ArithmeticException("Division by zero");
        return total / visitors;
    }
}
