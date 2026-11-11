package com.amalitech.dovewing.app;

import com.amalitech.dovewing.Depot;
import com.amalitech.dovewing.Economy;
import com.amalitech.dovewing.Premium;
import com.amalitech.dovewing.Rental;
import com.amalitech.dovewing.RentalRefusedException;
import com.amalitech.dovewing.Renter;
import com.amalitech.dovewing.RenterType;
import com.amalitech.dovewing.Vehicle;

import java.math.BigDecimal;

public final class Main {

    public static void main(String[] args) {
        Depot depot = new Depot();

        Vehicle vitz = new Vehicle("GT-1234-20", "Toyota Vitz",
                new BigDecimal("150.00"), new BigDecimal("500.00"), new Economy());
        Vehicle prado = new Vehicle("GR-5678-22", "Toyota Prado",
                new BigDecimal("600.00"), new BigDecimal("2000.00"), new Premium());
        depot.addVehicle(vitz);
        depot.addVehicle(prado);

        Renter kofi = new Renter(1, "Kofi Mensah", RenterType.CASUAL);
        Renter ama  = new Renter(2, "Ama Owusu",   RenterType.MEMBER);
        depot.addRenter(kofi);
        depot.addRenter(ama);

        tryRent(depot, kofi, vitz, 3);
        tryRent(depot, ama, prado, 2);
        tryReturn(depot, prado);
        tryRent(depot, ama, vitz, 2); // the Vitz is already out with Kofi: now refused

        System.out.println();
        System.out.println("=== Dovewing Car Rentals - end of day ===");
        Report.print(depot);
        System.out.println(depot.ledgerReport());
    }

    private static void tryRent(Depot depot, Renter renter, Vehicle vehicle, int days) {
        try {
            Rental r = depot.rent(renter.id(), vehicle.plate(), days);
            System.out.println("Rented " + vehicle.model() + " (" + vehicle.plate() + ") to " + renter.name()
                    + " for " + r.days() + " days, cost GHS " + Report.money(r.cost())
                    + ", deposit GHS " + Report.money(r.deposit()));
        } catch (RentalRefusedException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void tryReturn(Depot depot, Vehicle vehicle) {
        try {
            depot.returnVehicle(vehicle.plate());
            System.out.println("Returned " + vehicle.model() + " (" + vehicle.plate() + ")");
        } catch (RentalRefusedException e) {
            System.out.println(e.getMessage());
        }
    }
}