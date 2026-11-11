package com.amalitech.dovewing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The only class that changes rental state. It sees the fleet, renters and rentals together, so
 * it is the one place that checks "is it already rented?" before changing anything. It is not
 * thread-safe; use it from one thread or add locking before sharing it.
 */
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

    public Rental rent(int renterId, String plate, int days) throws RentalRefusedException {
        String key = Vehicle.normalise(plate);

        Vehicle v = fleet.get(key);
        if (v == null) {
            throw refuse(key, "unknown vehicle", "Unknown vehicle: " + plate);
        }

        Renter r = renters.get(renterId);
        if (r == null) {
            throw refuse(key, "unknown renter", "Unknown renter: " + renterId);
        }

        if (isRented(key)) {
            throw refuse(key, "already rented", "Vehicle already rented: " + v.plate());
        }

        if (days < 1) {
            throw refuse(key, "days must be >= 1", "days must be >= 1");
        }

        BigDecimal cost = v.category().cost(v.rate(), days);
        BigDecimal deposit = v.category().deposit(v.deposit(), r.type());

        Rental rental = new Rental(nextRentalId++, renterId, v.plate(), days, cost, deposit);
        rentals.add(rental);
        ledger.record(Ledger.Type.RENT, "plate=" + key + " renter=" + renterId + " days=" + days);
        return rental;
    }

    public void returnVehicle(String plate) throws RentalRefusedException {
        String key = Vehicle.normalise(plate);

        Vehicle v = fleet.get(key);
        if (v == null) {
            throw refuse(key, "unknown vehicle", "Unknown vehicle: " + plate);
        }

        Rental active = null;
        for (Rental x : rentals) {
            if (x.status() == RentalStatus.ACTIVE && Vehicle.normalise(x.plate()).equals(key)) {
                active = x;
                break;
            }
        }

        if (active == null) {
            throw refuse(key, "not rented", "Vehicle not rented: " + v.plate());
        }

        active.markReturned();
        ledger.record(Ledger.Type.RETURN, "plate=" + key);
    }

    public boolean isRented(String plate) {
        String key = Vehicle.normalise(plate);
        for (Rental x : rentals) {
            if (x.status() == RentalStatus.ACTIVE && Vehicle.normalise(x.plate()).equals(key)) {
                return true;
            }
        }
        return false;
    }

    public List<Rental> getRentals() { return List.copyOf(rentals); }

    public List<Rental> activeRentals() {
        List<Rental> active = new ArrayList<>();
        for (Rental x : rentals) {
            if (x.status() == RentalStatus.ACTIVE) {
                active.add(x);
            }
        }
        return active;
    }

    public List<Vehicle> fleet() { return List.copyOf(fleet.values()); }

    /** Returns the renter with this id, or null if there is none. */
    public Renter findRenter(int id) { return renters.get(id); }

    public String ledgerReport() { return ledger.report(rentals); }

    private RentalRefusedException refuse(String key, String reason, String message) {
        ledger.record(Ledger.Type.REFUSE, "plate=" + key + " reason=" + reason);
        return new RentalRefusedException(message);
    }
}