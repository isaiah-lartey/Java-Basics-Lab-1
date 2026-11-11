package com.amalitech.dovewing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Depot {

    private final Map<String, Vehicle> fleet = new LinkedHashMap<>();
    private final Map<Integer, Renter> renters = new LinkedHashMap<>();
    private final List<Rental> rentals = new ArrayList<>();
    private final Ledger ledger = new Ledger();
    private int nextRentalId = 1;

    public void addVehicle(Vehicle v) {
        String key = v.key();
        if (fleet.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate plate: " + v.plate());
        }
        fleet.put(key, v);
    }

    public void addRenter(Renter r) {
        if (renters.containsKey(r.id())) {
            throw new IllegalArgumentException("Duplicate renter id: " + r.id());
        }
        renters.put(r.id(), r);
    }

    public Rental rent(int renterId, String plate, int days) {
        String key = Vehicle.normalise(plate);

        Vehicle v = fleet.get(key);
        if (v == null) {
            ledger.record("REFUSE plate=" + key + " reason=unknown vehicle");
            throw new IllegalArgumentException("Unknown vehicle: " + plate);
        }

        Renter r = renters.get(renterId);
        if (r == null) {
            ledger.record("REFUSE plate=" + key + " reason=unknown renter");
            throw new IllegalArgumentException("Unknown renter: " + renterId);
        }

        if (isRented(key)) {
            ledger.record("REFUSE plate=" + key + " reason=already rented");
            throw new IllegalStateException("Vehicle already rented: " + v.plate());
        }

        if (days < 1) {
            ledger.record("REFUSE plate=" + key + " reason=days must be >= 1");
            throw new IllegalArgumentException("days must be >= 1");
        }

        BigDecimal cost = v.category().cost(v.rate(), days);
        BigDecimal deposit = v.category().deposit(v.deposit(), r.type());

        Rental rental = new Rental(nextRentalId++, renterId, v.plate(), days, cost, deposit);
        rentals.add(rental);
        ledger.record("RENT plate=" + key + " renter=" + renterId + " days=" + days);
        return rental;
    }

    public void returnVehicle(String plate) {
        String key = Vehicle.normalise(plate);

        Vehicle v = fleet.get(key);
        if (v == null) {
            ledger.record("REFUSE plate=" + key + " reason=unknown vehicle");
            throw new IllegalArgumentException("Unknown vehicle: " + plate);
        }

        Rental active = rentals.stream()
            .filter(x -> x.status() == RentalStatus.ACTIVE && Vehicle.normalise(x.plate()).equals(key))
            .findFirst()
            .orElse(null);

        if (active == null) {
            ledger.record("REFUSE plate=" + key + " reason=not rented");
            throw new IllegalStateException("Vehicle not rented: " + v.plate());
        }

        active.markReturned();
        ledger.record("RETURN plate=" + key);
    }

    public boolean isRented(String plate) {
        String key = Vehicle.normalise(plate);
        return rentals.stream()
            .anyMatch(x -> x.status() == RentalStatus.ACTIVE && Vehicle.normalise(x.plate()).equals(key));
    }

    public List<Rental> getRentals() { return List.copyOf(rentals); }

    public String ledgerReport() {
        return ledger.report(rentals);
    }

    public void printReport() {
        System.out.println("Fleet status:");
        for (Vehicle v : fleet.values()) {
            String state = isRented(v.plate()) ? "RENTED" : "AVAILABLE";
            System.out.println("  " + v.model() + " (" + v.plate() + ", " + v.category().name() + "): " + state);
        }
        System.out.println("Active rentals:");
        for (Rental r : rentals) {
            if (r.status() == RentalStatus.ACTIVE) {
                Renter renter = renters.get(r.renterId());
                String name = renter == null ? "unknown" : renter.name();
                System.out.println("  Rental #" + r.id() + ": " + r.plate() + " -> " + name + " (" + r.days() + " days)");
            }
        }
    }
}