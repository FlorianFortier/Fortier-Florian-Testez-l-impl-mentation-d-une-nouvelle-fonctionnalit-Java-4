package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
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
import java.text.ParseException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String REG_NUMBER = "ABCDEF";
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private static FareCalculatorService fareCalculatorService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    private static Ticket ticket;
    private static ParkingService parkingService;
    private static ParkingSpot parkingSpot;

    private static LocalDateTime outTime;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
        ticket = new Ticket();
        fareCalculatorService = new FareCalculatorService();
        parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        outTime = LocalDateTime.now(ZoneId.systemDefault()).plus(1, ChronoUnit.HOURS);
    }

    @BeforeEach
   public void setUpPerTest() throws Exception {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(REG_NUMBER);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        dataBasePrepareService.clearDataBaseEntries();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
    }

    @Test
    public void testParkingACar() throws SQLException, ClassNotFoundException, ParseException {
        // WHEN
        parkingService.processIncomingVehicle();
        // THEN
        try (Connection connection = dataBaseTestConfig.getConnection();
            PreparedStatement ps = connection.prepareStatement("select * from ticket where vehicle_reg_number=?")) {
            ps.setString(1, REG_NUMBER);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(rs.getInt("PRICE"), 0);
                assertEquals(rs.getString("VEHICLE_REG_NUMBER"), REG_NUMBER);
                assertEquals(rs.getInt("PARKING_NUMBER"), 1);
                LocalDateTime dateStockedBdd = LocalDateTime.ofInstant(rs.getTimestamp("IN_TIME").toInstant(), ZoneId.systemDefault());
                LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
                assertEquals(dateStockedBdd.truncatedTo(ChronoUnit.SECONDS), date);
                assertNull(rs.getTimestamp("OUT_TIME"));
            }
        }
        try (Connection connection = dataBaseTestConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement("select available from parking where parking_number=?")) {
            ps.setInt(1, 1);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(rs.getInt("available"), 0);
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testParkingLotExit() throws SQLException, ClassNotFoundException {
        parkingService.processExitingVehicle();
        try (Connection connection = dataBaseTestConfig.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ticket WHERE vehicle_reg_number=?")) {
            ps.setString(1, REG_NUMBER);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    ticket.setParkingSpot(parkingSpot);
                    ticket.setVehicleRegNumber(REG_NUMBER);
                    ticket.setInTime(Timestamp.valueOf(LocalDateTime.now(ZoneId.systemDefault())));
                    ticket.setOutTime(Timestamp.valueOf(outTime));
                    ticketDAO.updateTicket(ticket);
                    ticketDAO.saveTicket(ticket);
                    double fare = fareCalculatorService.calculateFare(ticket, false);



                    assertTrue(fare > 0);
                    assertTrue(ticket.getOutTime().getTime() > ticket.getInTime().getTime());
                    assertEquals(ticket.getPrice(), fare);
                } else {
                    throw new RuntimeException("No record found for vehicle number: " + REG_NUMBER);
                }
            }
        }
    }
    @Test
    public void testParkingLotExitRecurringUser() throws Exception, SQLException {
        // GIVEN
        try (Connection connection = dataBaseTestConfig.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ticket WHERE vehicle_reg_number=? ORDER BY in_time DESC LIMIT 1")) {
            ps.setString(1, REG_NUMBER);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
                    parkingSpotDAO.updateParking(parkingSpot);

                    LocalDateTime inTime = LocalDateTime.now().minusHours(1);
                    LocalDateTime outTime = LocalDateTime.now();
                    Ticket ticket = new Ticket();
                    ticket.setParkingSpot(parkingSpot);
                    ticket.setVehicleRegNumber(REG_NUMBER);
                    ticket.setPrice(0);
                    ticket.setInTime(Timestamp.valueOf(inTime));
                    ticket.setOutTime(Timestamp.valueOf(outTime));
                    ticketDAO.saveTicket(ticket);


                    ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

                    // when
                    ParkingSpot newParkingSpot = parkingService.getNextParkingNumberIfAvailable();
                    boolean isParkingSpotAvailable = newParkingSpot != null;

                    parkingService.processExitingVehicle();
                    Ticket updatedTicket = ticketDAO.getTicket(REG_NUMBER);

                    fareCalculatorService.calculateFare(updatedTicket, true);

                    // then
                    assertTrue(isParkingSpotAvailable);
                    assertNotNull(updatedTicket.getOutTime());
                    assertTrue(updatedTicket.getPrice() > 0);
                    assertEquals(updatedTicket.getPrice(), fareCalculatorService.calculateFare(updatedTicket, true));
                }
            }
        }

    }
}
