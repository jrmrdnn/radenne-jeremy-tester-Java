package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean isDiscount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect");
        }

        int discount = isDiscount ? Fare.DISCOUNT_RATE : 100;

        long durationInMilliseconds = ticket.getOutTime().getTime() - ticket.getInTime().getTime();
        int thirtyMinutesInMilliseconds = 30 * 60 * 1000;

        int CarRatePerHour = (int) (Fare.CAR_RATE_PER_HOUR * 100);
        int BikeRatePerHour = (int) (Fare.BIKE_RATE_PER_HOUR * 100);

        if (durationInMilliseconds < thirtyMinutesInMilliseconds) {
            ticket.setPrice(0);
            return;
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(calculatePrice(durationInMilliseconds, CarRatePerHour, discount));
                break;
            }
            case BIKE: {
                ticket.setPrice(calculatePrice(durationInMilliseconds, BikeRatePerHour, discount));
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    private double calculatePrice(long duration, int rate, int discount) {
        double durationInHours = duration / 3600000.0;
        double durationInRound = Math.round(durationInHours * 100.0) / 100.0;
        double price = durationInRound * rate * discount / 100;
        return price / 100;
    }
}