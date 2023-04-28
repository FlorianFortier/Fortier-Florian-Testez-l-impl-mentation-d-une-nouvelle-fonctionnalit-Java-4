package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;

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

    @BeforeEach
    public void setUp() {
      try {
        parkingService = new ParkingService(inputReaderUtil, parkingSpot, ticketDAO);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

      } catch(Exception e) {
        throw new IllegalArgumentException("Entered input is invalid");
      }
    }
  
  @Test
  public void testProcessExitingVehicle() {
      when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
      // When
      parkingService.processExitingVehicle();

      // Then
      verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
  }
  
   public void testProcessIncomingVehicle() {
    // Given
    when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

    // When
    parkingService.processIncomingVehicle();

    // Then
    verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
  }    

  @Test
  public void processExitingVehicleTestUnableUpdate() {
    

  }

  @Test
  public void testGetNextParkingNumberIfAvailable() {
    when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

    parkingService.getNextParkingNumberIfAvailable();

    verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
  }
  @Test

  public void testGetNextParkingNumberIfAvailableParkingNumberNotFound()  {

    

    parkingService.getNextParkingNumberIfAvailable();


  }
  @Test 
  public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    
    
    parkingService.getNextParkingNumberIfAvailable();

    
  } 


}