package com.parkit.parkingsystem.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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

        double discount = isDiscount ? Fare.DISCOUNT_RATE : 1;

        long durationInMinutes = convertToMinutes(ticket.getInTime(), ticket.getOutTime());

        int thirtyMinutes = 30;

        double price = 0;

        if (durationInMinutes >= thirtyMinutes) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    price = calculatePrice(durationInMinutes, Fare.CAR_RATE_PER_HOUR, discount);
                    break;
                }
                case BIKE: {
                    price = calculatePrice(durationInMinutes, Fare.BIKE_RATE_PER_HOUR, discount);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }

        ticket.setPrice(price);
    }

    private long convertToMinutes(Date inTime, Date outTime) {
        LocalDateTime inLocalDateTime = inTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime outLocalDateTime = outTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return ChronoUnit.MINUTES.between(inLocalDateTime, outLocalDateTime);
    }

    private double calculatePrice(long durationInMinutes, double fareInHours, double discount) {
        double durationInHours = durationInMinutes / 60.0;
        double calculate = durationInHours * fareInHours * discount;
        return Math.round(calculate * 100.0) / 100.0;
    }
}