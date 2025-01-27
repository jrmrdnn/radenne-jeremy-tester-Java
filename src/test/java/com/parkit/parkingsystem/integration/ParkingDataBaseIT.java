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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingService parkingService;
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
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

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
        parkingService.processIncomingVehicle();

        assertNotNull(ticketDAO.getTicket(vehicleRegNumber).getInTime());
        assertNotNull(ticketDAO.getTicket(vehicleRegNumber).getParkingSpot());
        assertFalse(ticketDAO.getTicket(vehicleRegNumber).getParkingSpot().isAvailable());
        assertEquals(vehicleRegNumber, ticketDAO.getTicket(vehicleRegNumber).getVehicleRegNumber());
    }

    @Test
    public void testParkingLotExit() {
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        assertEquals(0.0, ticketDAO.getTicket(vehicleRegNumber).getPrice());
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

        parkingService.processIncomingVehicle();

        Ticket newTicket = ticketDAO.getTicket(vehicleRegNumber);
        newTicket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticketDAO.updateTicket(newTicket);

        parkingService.processExitingVehicle();

        assertEquals(2, ticketDAO.getNbTicket(vehicleRegNumber));
        double price = Fare.CAR_RATE_PER_HOUR * Fare.DISCOUNT_RATE / 100;
        assertEquals(price, ticketDAO.getTicket(vehicleRegNumber).getPrice());
    }
}
