package com.amalitech.dovewing.app;

import com.amalitech.dovewing.Depot;
import com.amalitech.dovewing.Rental;
import com.amalitech.dovewing.Renter;
import com.amalitech.dovewing.Vehicle;

import java.math.BigDecimal;

/** Presentation only. Reads the depot through its public, read-only views and prints. */
public final class Report {

    private Report() { }

    public static void print(Depot depot) {
        System.out.println("Fleet status:");
        for (Vehicle v : depot.fleet()) {
            String state = depot.isRented(v.plate()) ? "RENTED" : "AVAILABLE";
            System.out.println("  " + v.model() + " (" + v.plate() + ", " + v.category().name() + "): " + state);
        }
        System.out.println("Active rentals:");
        for (Rental r : depot.activeRentals()) {
            Renter renter = depot.findRenter(r.renterId());
            String name = renter == null ? "unknown" : renter.name();
            System.out.println("  Rental #" + r.id() + ": " + r.plate() + " -> " + name + " (" + r.days() + " days)");
        }
    }

    /** Prints money the way the original did: 450.0, 0.0, 450.5. */
    public static String money(BigDecimal amount) {
        String s = amount.stripTrailingZeros().toPlainString();
        if (!s.contains(".")) return s + ".0";
        return s;
    }
}