package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static final String vehicleRegNumber = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegNumber);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

        assertNotNull(ticket);
        assertEquals(vehicleRegNumber, ticket.getVehicleRegNumber());
        assertFalse(ticket.getParkingSpot().isAvailable());
    }

    @Test
    public void testParkingLotExit() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setOutTime(new Date());
        ticketDAO.updateTicket(ticket);

        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket(vehicleRegNumber);

        assertNotNull(ticket.getOutTime());
        System.out.println(ticket.getPrice());
        assertTrue(ticket.getPrice() > 0);
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 3000)));
        ticket.setOutTime(new Date(System.currentTimeMillis() - (60 * 60 * 2000)));
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));

        ticketDAO.saveTicket(ticket);

        int nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);
        assertEquals(1, nbTickets);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        Ticket newTicket = ticketDAO.getTicket(vehicleRegNumber);
        newTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        newTicket.setOutTime(new Date());
        ticketDAO.updateTicket(newTicket);

        parkingService.processExitingVehicle();

        nbTickets = ticketDAO.getNbTicket(vehicleRegNumber);
        assertEquals(2, nbTickets);

        newTicket = ticketDAO.getTicket(vehicleRegNumber);
        assertEquals(Fare.DISCOUNT_RATE * Fare.CAR_RATE_PER_HOUR, newTicket.getPrice(), 0.001);
    }
}
