package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;


public class FareCalculatorService {

 public double calculateFare(Ticket ticket, boolean discount) {
    if (ticket.getOutTime() == null || ticket.getOutTime().before(ticket.getInTime())) {
        throw new IllegalArgumentException("Incorrect Out Time: " + ticket.getOutTime());
    }
    long inTimeMilliseconds = ticket.getInTime().getTime();
    long outTimeMilliseconds = ticket.getOutTime().getTime();
    long durationInMilliseconds = outTimeMilliseconds - inTimeMilliseconds;
    double durationInHours = (double) durationInMilliseconds / (1000 * 60 * 60);
    long durationInMinutes = durationInMilliseconds / (1000 * 60);

    switch (ticket.getParkingSpot().getParkingType()) {
        case CAR:
            if (durationInMinutes <= 30) {
              ticket.setPrice(0);
            } else {
              ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
            }
            break;
        case BIKE:
            if (durationInMinutes <= 30) {
              ticket.setPrice(0);
            } else {
              ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown Parking Type");
    }
    if (discount) {
      ticket.setPrice(ticket.getPrice() * 0.95);
    }
     return ticket.getPrice();
 }
  public void calculateFare(Ticket ticket) {
    calculateFare(ticket, false);
    
  }
}