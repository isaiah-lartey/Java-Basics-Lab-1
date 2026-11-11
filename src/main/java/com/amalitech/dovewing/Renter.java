package com.amalitech.dovewing;

public record Renter(int id, String name, RenterType type) {

    public Renter {
        if (id <= 0) throw new IllegalArgumentException("id must be > 0");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (type == null) throw new IllegalArgumentException("type required");
    }
}