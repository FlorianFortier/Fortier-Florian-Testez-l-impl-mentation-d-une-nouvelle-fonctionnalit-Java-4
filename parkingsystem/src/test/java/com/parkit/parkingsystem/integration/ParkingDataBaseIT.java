package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String REG_NUMBER = null;
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    private static FareCalculatorService fareCalculatorService;
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() throws SQLException, ClassNotFoundException {
            // GIVEN
            ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            // WHEN
            parkingService.processIncomingVehicle();
            // THEN
            try (Connection connection = dataBaseTestConfig.getConnection();
                 PreparedStatement ps = connection.prepareStatement("select * from ticket where vehicle_reg_number=?")) {
                ps.setString(1, REG_NUMBER);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs   .next());
                    assertEquals(rs.getInt("PRICE"), 0);
                    assertEquals(rs.getInt("DISCOUNT"), 0);
                    assertEquals(rs.getString("VEHICLE_REG_NUMBER"), REG_NUMBER);
                    assertEquals(rs.getInt("PARKING_NUMBER"), 1);
                    assertEquals(rs.getTimestamp("IN_TIME"), new Date());
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
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    @Test
    public void testParkingLotExit() throws SQLException, ClassNotFoundException {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        LocalDateTime outTime = LocalDateTime.now();

        // when
        double fare = fareCalculatorService.calculateFare(ticket,false);

        // then
        assertTrue(fare > 0);
        assertEquals(outTime.minusHours(1), ticket.getOutTime());
    }

}
