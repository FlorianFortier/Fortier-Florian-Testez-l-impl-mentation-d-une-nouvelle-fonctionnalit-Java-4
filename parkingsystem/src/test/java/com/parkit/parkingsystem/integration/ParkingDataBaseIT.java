package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;

import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String REG_NUMBER = "ABCDEF";
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static FareCalculatorService fareCalculatorService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    private static Ticket ticket;
    private static ParkingService parkingService;
    private static LocalDateTime outTime;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        ticketDAO = new TicketDAO();
        dataBasePrepareService = new DataBasePrepareService();
        ticket = new Ticket();
        fareCalculatorService = new FareCalculatorService();
        outTime = LocalDateTime.now(ZoneId.systemDefault()).plus(2, ChronoUnit.HOURS);

    }

    @BeforeEach
   public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(REG_NUMBER);
        dataBasePrepareService.clearDataBaseEntries();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void testParkingACar() {
        // WHEN

        int slotBefore = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        // ACT
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(REG_NUMBER);

        ParkingSpot parkingSpot = ticket.getParkingSpot();
        int SlotAfter = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);


        // ASSERT
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertEquals(parkingSpot.getId(),slotBefore);
        assertNotEquals(SlotAfter,slotBefore);
}
    @Test
    public void testParkingLotExit() {
        // GIVEN
        parkingService.processIncomingVehicle();

        ticket = ticketDAO.getTicket(REG_NUMBER);

        ticket.setOutTime(Timestamp.valueOf(outTime));

        ticketDAO.updateTicket(ticket);

        // WHEN

        ParkingSpot parkingSpot = ticket.getParkingSpot();
        int slotBefore = parkingSpot.getId();
        int SlotAfter = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        ticket = ticketDAO.getTicket(REG_NUMBER);

        // THEN
        double fare = fareCalculatorService.calculateFare(ticket, false);

        parkingService.processExitingVehicle();

        assertEquals(parkingSpot.getId(),slotBefore);
        assertNotEquals(SlotAfter,slotBefore);
        assertEquals(1, ticket.getParkingSpot().getId());
        assertNotNull(ticket.getOutTime());
        assertNotEquals(ticket.getPrice(), 0);
    }
    @Test
    public void testParkingLotExitRecurringUser() throws Exception, SQLException {
        // GIVEN
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        ticket = ticketDAO.getTicket(REG_NUMBER);

        // WHEN
        ParkingSpot newParkingSpot = parkingService.getNextParkingNumberIfAvailable();
        boolean isParkingSpotAvailable = newParkingSpot != null;
        Ticket updatedTicket = ticketDAO.getTicket(REG_NUMBER);
        updatedTicket.setOutTime(Timestamp.valueOf(outTime));
        updatedTicket.setPrice(0);
        updatedTicket.setInTime(Timestamp.valueOf(outTime.minus(1, ChronoUnit.HOURS)));
        updatedTicket.setParkingSpot(newParkingSpot);

        ticketDAO.updateTicket(updatedTicket);

        parkingService.processExitingVehicle();

        fareCalculatorService.calculateFare(updatedTicket, true);

        // THEN
        assertTrue(isParkingSpotAvailable);
        assertNotNull(updatedTicket.getOutTime());
        assertTrue(updatedTicket.getPrice() > 0);
        assertEquals(updatedTicket.getPrice(), fareCalculatorService.calculateFare(updatedTicket, true));
    }
}
