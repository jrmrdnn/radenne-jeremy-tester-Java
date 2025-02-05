package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {

    @InjectMocks
    private TicketDAO ticketDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private Ticket ticket;

    @BeforeEach
    public void setUp() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABC123");
        ticket.setPrice(10.0);
        ticket.setInTime(new Date());
        ticket.setOutTime(new Date());

        when(dataBaseConfig.getConnection()).thenReturn(connection);
    }

    @Test
    public void testSaveTicket() throws Exception {
        when(connection.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(preparedStatement);
        when(preparedStatement.execute()).thenReturn(true);

        boolean result = ticketDAO.saveTicket(ticket);
        assertTrue(result);

        verify(preparedStatement, times(1)).setInt(1, ticket.getParkingSpot().getId());
        verify(preparedStatement, times(1)).setString(2, ticket.getVehicleRegNumber());
        verify(preparedStatement, times(1)).setDouble(3, ticket.getPrice());
        verify(preparedStatement, times(1)).setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
        verify(preparedStatement, times(1)).setTimestamp(5, new Timestamp(ticket.getOutTime().getTime()));
        verify(preparedStatement, times(1)).execute();
    }

    @Test
    public void testGetTicket() throws Exception {
        when(connection.prepareStatement(DBConstants.GET_TICKET)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        lenient().when(resultSet.getInt(1)).thenReturn(1);
        lenient().when(resultSet.getInt(2)).thenReturn(1);
        lenient().when(resultSet.getString(6)).thenReturn("CAR");
        lenient().when(resultSet.getDouble(3)).thenReturn(10.0);
        lenient().when(resultSet.getTimestamp(4)).thenReturn(new Timestamp(ticket.getInTime().getTime()));
        lenient().when(resultSet.getTimestamp(5)).thenReturn(new Timestamp(ticket.getOutTime().getTime()));

        Ticket result = ticketDAO.getTicket("ABC123");
        assertNotNull(result);

        assertEquals(ticket.getParkingSpot().getId(), result.getParkingSpot().getId());
        assertEquals(ticket.getVehicleRegNumber(), result.getVehicleRegNumber());
        assertEquals(ticket.getPrice(), result.getPrice());
        assertEquals(ticket.getInTime(), result.getInTime());
        assertEquals(ticket.getOutTime(), result.getOutTime());
    }

    @Test
    public void testUpdateTicket() throws Exception {
        when(connection.prepareStatement(DBConstants.UPDATE_TICKET)).thenReturn(preparedStatement);

        boolean result = ticketDAO.updateTicket(ticket);
        assertTrue(result);

        verify(preparedStatement, times(1)).setDouble(1, ticket.getPrice());
        verify(preparedStatement, times(1)).setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
        verify(preparedStatement, times(1)).setTimestamp(3, new Timestamp(ticket.getInTime().getTime()));
        verify(preparedStatement, times(1)).setInt(4, ticket.getId());
        verify(preparedStatement, times(1)).execute();
    }

    @Test
    public void testGetNbTicket() throws Exception {
        when(connection.prepareStatement(DBConstants.GET_NUMBER_TICKET)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        int result = ticketDAO.getNbTicket("ABC123");

        assertEquals(5, result);
    }
}