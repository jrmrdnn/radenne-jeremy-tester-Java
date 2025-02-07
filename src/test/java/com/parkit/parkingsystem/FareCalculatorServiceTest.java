package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

public class FareCalculatorServiceTest {
    private Ticket ticket;

    private static long currentTimeMillis;

    private static Date twentyFourHours;
    private static Date oneHour;
    private static Date fortyFiveMinutes;
    private static Date twentyNineMinutes;
    private static Date now;

    private static ParkingSpot carParkingSpot;
    private static ParkingSpot bikeParkingSpot;

    @BeforeAll
    public static void setUp() {
        currentTimeMillis = System.currentTimeMillis();

        now = new Date(currentTimeMillis);
        twentyFourHours = new Date(currentTimeMillis - (24 * 60 * 60 * 1000));
        oneHour = new Date(currentTimeMillis - (60 * 60 * 1000));
        fortyFiveMinutes = new Date(currentTimeMillis - (45 * 60 * 1000));
        twentyNineMinutes = new Date(currentTimeMillis - (29 * 60 * 1000));

        carParkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        bikeParkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(now);
        ticket.setParkingSpot(carParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareBike() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(now);
        ticket.setParkingSpot(bikeParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareUnknownType() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(now);
        ticket.setParkingSpot(new ParkingSpot(1, null, false));
        assertThrows(NullPointerException.class, () -> new FareCalculatorService().calculateFare(ticket));
    }

    @Test
    public void calculateFareCarWithFutureInTime() {
        ticket.setInTime(now);
        ticket.setOutTime(oneHour);
        ticket.setParkingSpot(carParkingSpot);
        assertThrows(IllegalArgumentException.class, () -> new FareCalculatorService().calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        ticket.setInTime(now);
        ticket.setOutTime(oneHour);
        ticket.setParkingSpot(bikeParkingSpot);
        assertThrows(IllegalArgumentException.class, () -> new FareCalculatorService().calculateFare(ticket));
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        ticket.setInTime(fortyFiveMinutes);
        ticket.setOutTime(now);
        ticket.setParkingSpot(carParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice(), 0.01);
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        ticket.setInTime(fortyFiveMinutes);
        ticket.setOutTime(now);
        ticket.setParkingSpot(bikeParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice(), 0.01);
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        ticket.setInTime(twentyFourHours);
        ticket.setOutTime(now);
        ticket.setParkingSpot(carParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithMoreThanADayParkingTime() {
        ticket.setInTime(twentyFourHours);
        ticket.setOutTime(now);
        ticket.setParkingSpot(bikeParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals((24 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        ticket.setInTime(twentyNineMinutes);
        ticket.setOutTime(now);
        ticket.setParkingSpot(carParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        ticket.setInTime(twentyNineMinutes);
        ticket.setOutTime(now);
        ticket.setParkingSpot(bikeParkingSpot);
        new FareCalculatorService().calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithDiscount() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(now);
        ticket.setParkingSpot(carParkingSpot);
        new FareCalculatorService().calculateFare(ticket, true);
        assertEquals(Fare.CAR_RATE_PER_HOUR * Fare.DISCOUNT_RATE, ticket.getPrice(), 0.01);
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(now);
        ticket.setParkingSpot(bikeParkingSpot);
        new FareCalculatorService().calculateFare(ticket, true);
        assertEquals(Fare.BIKE_RATE_PER_HOUR * Fare.DISCOUNT_RATE, ticket.getPrice(), 0.01);
    }

    @Test
    public void calculateFareWithNullOutTime() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(null);
        ticket.setParkingSpot(carParkingSpot);
        assertThrows(IllegalArgumentException.class, () -> new FareCalculatorService().calculateFare(ticket));
    }

    @Test
    public void calculateFareWithNullInTime() {
        ticket.setInTime(null);
        ticket.setOutTime(now);
        ticket.setParkingSpot(carParkingSpot);
        assertThrows(NullPointerException.class, () -> new FareCalculatorService().calculateFare(ticket));
    }

    @Test
    public void calculateFareWithNullParkingSpot() {
        ticket.setInTime(oneHour);
        ticket.setOutTime(now);
        ticket.setParkingSpot(null);
        assertThrows(NullPointerException.class, () -> new FareCalculatorService().calculateFare(ticket));
    }
}