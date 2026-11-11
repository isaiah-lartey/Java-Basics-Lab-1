package com.amalitech.dovewing;

import java.math.BigDecimal;
import java.util.Locale;

public record Vehicle(String plate, String model, BigDecimal rate, BigDecimal deposit, Category category) {

    public Vehicle {
        if (plate == null || plate.isBlank()) throw new IllegalArgumentException("plate required");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model required");
        if (rate == null) throw new IllegalArgumentException("rate required");
        if (rate.signum() <= 0) throw new IllegalArgumentException("rate must be > 0");
        if (deposit == null) throw new IllegalArgumentException("deposit required");
        if (deposit.signum() < 0) throw new IllegalArgumentException("deposit must be >= 0");
        if (category == null) throw new IllegalArgumentException("category required");
    }

    public String key() { return normalise(plate); }

    public static String normalise(String plate) {
        return plate == null ? "" : plate.trim().toUpperCase(Locale.ROOT);
    }
}