package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private Date inTime;
    private Date outTime;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        inTime = new Date();
        outTime = new Date();

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
    }

    @Test
    public void calculateFareCar() {

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);
        double expectedPrice = Duration.between(
                LocalDateTime.ofInstant(inTime.toInstant(), ZoneId.systemDefault()).toLocalTime(),
                LocalDateTime.ofInstant(outTime.toInstant(), ZoneId.systemDefault()).toLocalTime()
        ).toHours() * Fare.CAR_RATE_PER_HOUR;
        assertEquals(expectedPrice, ticket.getPrice(), 0.01);

    }

    @Test
    public void calculateFareBike() {
        inTime.setTime(inTime.getTime() - (60 * 60 * 1000));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType() {
        inTime.setTime(inTime.getTime() - (60 * 60 * 1000));

        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);


        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        inTime.setTime(inTime.getTime() + (60 * 60 * 1000));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        inTime.setTime(inTime.getTime() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        inTime.setTime(inTime.getTime() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        inTime.setTime(inTime.getTime() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        inTime.setTime(inTime.getTime() - (30 * 60 * 1000));//under 30 minutes parking time should give free parking fare
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());

    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        inTime.setTime(inTime.getTime() - (30 * 60 * 1000));//under 30 minutes parking time should give free parking fare
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket);

        assertEquals(0, ticket.getPrice());

    }

    @Test
    public void calculateFareCarWithDiscount() {
        inTime.setTime(inTime.getTime() - (60 * 60 * 1000));// 1 hour parking time
        double expectedPrice = Fare.CAR_RATE_PER_HOUR * 1 * 0.95; // 5% discount
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, true);


        assertEquals(expectedPrice, ticket.getPrice()); // expected price is 5% less than the original price
    }

    @Test
    public void calculateFareBikeWithDiscount() {
        inTime.setTime(inTime.getTime() - (60 * 60 * 1000));// 1 hour parking time
        double expectedPrice = 0.95; // 5% discount
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);
        ticket.setParkingSpot(parkingSpot);

        fareCalculatorService.calculateFare(ticket, true);

        assertEquals(expectedPrice, ticket.getPrice()); // expected price is 5% less than the original price
    }
}
