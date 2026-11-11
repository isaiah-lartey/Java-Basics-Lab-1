package com.amalitech.dovewing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Ledger {

    enum Type { RENT, RETURN, REFUSE }

    private record Entry(Instant at, Type type, String detail) {
        String line() { return at + " " + type + " " + detail; }
    }

    private final List<Entry> entries = new ArrayList<>();

    void record(Type type, String detail) {
        if (type == null) throw new IllegalArgumentException("type required");
        if (detail == null || detail.isBlank()) throw new IllegalArgumentException("detail required");
        entries.add(new Entry(Instant.now(), type, detail));
    }

    public List<String> entries() {
        List<String> lines = new ArrayList<>();
        for (Entry e : entries) {
            lines.add(e.line());
        }
        return List.copyOf(lines);
    }

    public int size() { return entries.size(); }

    private int count(Type type) {
        int n = 0;
        for (Entry e : entries) {
            if (e.type() == type) n++;
        }
        return n;
    }

    private static int countRentals(List<Rental> rentals, RentalStatus status) {
        int n = 0;
        for (Rental r : rentals) {
            if (r.status() == status) n++;
        }
        return n;
    }

    public String report(List<Rental> rentals) {
        int rents = count(Type.RENT);
        int returns = count(Type.RETURN);
        int returned = countRentals(rentals, RentalStatus.RETURNED);
        int active = countRentals(rentals, RentalStatus.ACTIVE);

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