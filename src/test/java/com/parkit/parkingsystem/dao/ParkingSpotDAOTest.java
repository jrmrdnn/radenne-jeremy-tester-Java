package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingSpotDAOTest {

    @InjectMocks
    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws Exception {
        when(dataBaseConfig.getConnection()).thenReturn(connection);
    }

    @Test
    public void testGetNextAvailableSlot() throws Exception {
        when(connection.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        int slot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(1, slot);
        verify(connection, times(1)).prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
        verify(preparedStatement, times(1)).setString(1, ParkingType.CAR.toString());
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        verify(resultSet, times(1)).getInt(1);
        verify(dataBaseConfig, times(1)).closeResultSet(resultSet);
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateParking() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(result);
        verify(connection, times(1)).prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
        verify(preparedStatement, times(1)).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement, times(1)).setInt(2, parkingSpot.getId());
        verify(preparedStatement, times(1)).executeUpdate();
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }

    @Test
    public void testUpdateParkingFailure() throws Exception {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean result = parkingSpotDAO.updateParking(parkingSpot);

        assertFalse(result);
        verify(connection, times(1)).prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
        verify(preparedStatement, times(1)).setBoolean(1, parkingSpot.isAvailable());
        verify(preparedStatement, times(1)).setInt(2, parkingSpot.getId());
        verify(preparedStatement, times(1)).executeUpdate();
        verify(dataBaseConfig, times(1)).closePreparedStatement(preparedStatement);
        verify(dataBaseConfig, times(1)).closeConnection(connection);
    }
}