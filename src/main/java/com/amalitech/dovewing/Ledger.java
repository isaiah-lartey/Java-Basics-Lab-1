package com.amalitech.dovewing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Ledger {

    private final List<String> entries = new ArrayList<>();

    void record(String event) {
        if (event == null || event.isBlank()) throw new IllegalArgumentException("event required");
        entries.add(Instant.now() + " " + event);
    }

    public List<String> entries() { return List.copyOf(entries); }

    public int size() { return entries.size(); }

    public String report(List<Rental> rentals) {
        long rents = entries.stream().filter(e -> e.contains(" RENT ")).count();
        long returns = entries.stream().filter(e -> e.contains(" RETURN ")).count();
        long active = rentals.stream().filter(r -> r.status() == RentalStatus.ACTIVE).count();
        long returned = rentals.stream().filter(r -> r.status() == RentalStatus.RETURNED).count();

        if (rents != rentals.size()) {
            throw new IllegalStateException(
                "Ledger and live state disagree: " + rents + " rents, " + rentals.size() + " total rentals");
        }
        if (returns != returned) {
            throw new IllegalStateException(
                "Ledger and live state disagree: " + returns + " returns, " + returned + " returned rentals");
        }

        return "Ledger:\n"
             + "  events: " + entries.size() + "\n"
             + "  active rentals: " + active + "\n"
             + "  status: OK";
    }
}