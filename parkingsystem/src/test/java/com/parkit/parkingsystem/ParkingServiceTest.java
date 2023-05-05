package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterEach;
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
    private Ticket ticket;
    @BeforeEach
    public void setUp() {
      try {
        parkingService = new ParkingService(inputReaderUtil, parkingSpot, ticketDAO);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setPrice(0);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
        ticketDAO.saveTicket(ticket);





      } catch(Exception e) {
        throw new IllegalArgumentException("Entered input is invalid");
      }
    }
    @AfterEach
    public void tearDown() {
        parkingService = null;
        inputReaderUtil = null;
        parkingSpot = null;
        ticketDAO = null;
        ticket = null;
    }
  @Test
  public void testProcessExitingVehicle() {
    //Given

    when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

    // When
    parkingService.processExitingVehicle();

    // Then
    verify(ticketDAO, times(1)).getNbTicket();
  }
  @Test
   public void testProcessIncomingVehicle() throws Exception {

    when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);


    parkingService.processIncomingVehicle();

    verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));

  }    

  @Test
  public void processExitingVehicleTestUnableUpdate() {

    when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

    parkingService.processExitingVehicle();

    verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
  }

  @Test
  public void testGetNextParkingNumberIfAvailable() {

    when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

    parkingService.getNextParkingNumberIfAvailable();

    verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
  }
  @Test

  public void testGetNextParkingNumberIfAvailableParkingNumberNotFound()  {

    when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);

    parkingService.getNextParkingNumberIfAvailable();

    verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));

  }
  @Test
  public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    when(parkingSpot.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

    parkingService.getNextParkingNumberIfAvailable();

    verify(parkingSpot, times(1)).getNextAvailableSlot(any(ParkingType.class));
    
  } 


}