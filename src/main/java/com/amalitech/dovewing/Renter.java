package com.amalitech.dovewing;

public final class Renter {

    private final int id;
    private final String name;
    private final RenterType type;

    public Renter(int id, String name, RenterType type) {
        if (id <= 0) throw new IllegalArgumentException("id must be > 0");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (type == null) throw new IllegalArgumentException("type required");
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int id() { return id; }
    public String name() { return name; }
    public RenterType type() { return type; }
}