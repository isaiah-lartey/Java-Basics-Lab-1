package com.amalitech.dovewing;

import java.math.BigDecimal;

public final class Rental {

    private final int id;
    private final int renterId;
    private final String plate;
    private final int days;
    private final BigDecimal cost;
    private final BigDecimal deposit;
    private RentalStatus status;

    public Rental(int id, int renterId, String plate, int days, BigDecimal cost, BigDecimal deposit) {
        if (id <= 0) throw new IllegalArgumentException("id must be > 0");
        if (renterId <= 0) throw new IllegalArgumentException("renterId must be > 0");
        if (plate == null || plate.isBlank()) throw new IllegalArgumentException("plate required");
        if (days < 1) throw new IllegalArgumentException("days must be >= 1");
        if (cost == null) throw new IllegalArgumentException("cost required");
        if (cost.signum() < 0) throw new IllegalArgumentException("cost must be >= 0");
        if (deposit == null) throw new IllegalArgumentException("deposit required");
        if (deposit.signum() < 0) throw new IllegalArgumentException("deposit must be >= 0");
        this.id = id;
        this.renterId = renterId;
        this.plate = plate;
        this.days = days;
        this.cost = cost;
        this.deposit = deposit;
        this.status = RentalStatus.ACTIVE;
    }

    public int id() { return id; }
    public int renterId() { return renterId; }
    public String plate() { return plate; }
    public int days() { return days; }
    public BigDecimal cost() { return cost; }
    public BigDecimal deposit() { return deposit; }
    public RentalStatus status() { return status; }

    void markReturned() {
        if (status == RentalStatus.RETURNED) {
            throw new IllegalStateException("Rental " + id + " is already returned");
        }
        this.status = RentalStatus.RETURNED;
    }
}