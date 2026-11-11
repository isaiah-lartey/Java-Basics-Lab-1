package com.amalitech.dovewing;

import java.math.BigDecimal;
import java.util.Locale;

public final class Vehicle {

    private final String plate;
    private final String model;
    private final BigDecimal rate;
    private final BigDecimal deposit;
    private final Category category;

    public Vehicle(String plate, String model, BigDecimal rate, BigDecimal deposit, Category category) {
        if (plate == null || plate.isBlank()) throw new IllegalArgumentException("plate required");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model required");
        if (rate == null) throw new IllegalArgumentException("rate required");
        if (deposit == null) throw new IllegalArgumentException("deposit required");
        if (category == null) throw new IllegalArgumentException("category required");
        this.plate = plate;
        this.model = model;
        this.rate = rate;
        this.deposit = deposit;
        this.category = category;
    }

    public String plate()  { return plate; }
    public String model()  { return model; }
    public BigDecimal rate()    { return rate; }
    public BigDecimal deposit() { return deposit; }
    public Category category()  { return category; }

    public String key() { return normalise(plate); }

    public static String normalise(String plate) {
        return plate == null ? "" : plate.trim().toUpperCase(Locale.ROOT);
    }
}