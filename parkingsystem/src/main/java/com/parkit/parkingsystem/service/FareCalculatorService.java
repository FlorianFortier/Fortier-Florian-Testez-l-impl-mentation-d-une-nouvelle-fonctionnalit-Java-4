package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

 public void calculateFare(Ticket ticket) {
    if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
        throw new IllegalArgumentException("Incorrect Out Time: " + ticket.getOutTime());
    }
    long inTimeMilliseconds = ticket.getInTime().getTime();
    long outTimeMilliseconds = ticket.getOutTime().getTime();
    long durationInMilliseconds = outTimeMilliseconds - inTimeMilliseconds;
    double durationInHours = (double) durationInMilliseconds / (1000 * 60 * 60);
    switch (ticket.getParkingSpot().getParkingType()) {
        case CAR:
            ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
            break;
        case BIKE:
            ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
            break;
        default:
            throw new IllegalArgumentException("Unknown Parking Type");
    }
  }
}