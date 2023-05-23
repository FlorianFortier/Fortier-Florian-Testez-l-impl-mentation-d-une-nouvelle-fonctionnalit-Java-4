package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {


    private static ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpot;
    @Mock
    private static TicketDAO ticketDAO;

    @Mock
    private Ticket ticket;

    @BeforeEach
    public void setUp() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpot, ticketDAO);
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket = new Ticket();
            ticket.setVehicleRegNumber("ABCDEF");
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setPrice(0);


        } catch (Exception e) {
            throw new IllegalArgumentException("Entered input is invalid");
        }
    }

    @Test
    public void testProcessExitingVehicle() throws Exception {
        //Given
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(ticketDAO.getNbTicket()).thenReturn(1);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);


        // When
        parkingService.processExitingVehicle();
        // Then
        verify(ticketDAO, times(1)).getNbTicket();
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getTicket(anyString());

    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {

        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getTicket(anyString());
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
    }

    @Test

    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

        parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, times(1)).readSelection();

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(3);

        parkingService.getNextParkingNumberIfAvailable();

        verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(inputReaderUtil, times(1)).readSelection();
    }


}