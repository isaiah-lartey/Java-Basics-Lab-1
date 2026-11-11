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
        return entries.stream().map(Entry::line).toList();
    }

    public int size() { return entries.size(); }

    private long count(Type type) {
        return entries.stream().filter(e -> e.type() == type).count();
    }

    public String report(List<Rental> rentals) {
        long rents = count(Type.RENT);
        long returns = count(Type.RETURN);
        long returned = rentals.stream().filter(r -> r.status() == RentalStatus.RETURNED).count();
        long active = rentals.stream().filter(r -> r.status() == RentalStatus.ACTIVE).count();

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