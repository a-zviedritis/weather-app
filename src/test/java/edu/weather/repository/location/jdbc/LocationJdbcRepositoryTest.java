package edu.weather.repository.location.jdbc;

import edu.weather.service.location.ipstack.model.LocationResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsertOperations;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static edu.weather.repository.location.jdbc.DBSchema.GeolocationAccessTable.COLUMN_TIMESTAMP;
import static edu.weather.repository.location.jdbc.DBSchema.GeolocationTable.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LocationJdbcRepositoryTest {

    private static final String IP = "1.1.1.1";

    private static LocationJdbcRepository repository;
    private static SimpleJdbcInsertOperations locationInsertMock;
    private static SimpleJdbcInsertOperations locationAccessInsertMock;
    private static NamedParameterJdbcOperations parameterJdbcOperationsMock;
    private static SqlRowSet rsMock;

    @BeforeAll
    public static void beforeAll() {
        locationInsertMock = mock(SimpleJdbcInsertOperations.class);
        locationAccessInsertMock = mock(SimpleJdbcInsertOperations.class);
        parameterJdbcOperationsMock = mock(NamedParameterJdbcOperations.class);

        repository = new LocationJdbcRepository(mock(DataSource.class));
        ReflectionTestUtils.setField(repository, "locationInsert", locationInsertMock);
        ReflectionTestUtils.setField(repository, "locationAccessInsert", locationAccessInsertMock);
        ReflectionTestUtils.setField(repository, "parameterJdbcOperations", parameterJdbcOperationsMock);

        rsMock = mock(SqlRowSet.class);
    }

    @BeforeEach
    public void beforeEach() {
        reset(locationInsertMock, locationAccessInsertMock, parameterJdbcOperationsMock, rsMock);
    }

    @Test
    public void testLocationExistsTrue() throws Exception {
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);

        assertThat(repository.locationExists(IP)).isTrue();

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), captor.capture());
        verify(rsMock, times(1)).next();
        verifyNoMoreInteractions(parameterJdbcOperationsMock, rsMock);
        verifyNoInteractions(locationInsertMock, locationAccessInsertMock);

        MapSqlParameterSource caughtParams = captor.getValue();
        assertThat(caughtParams).isNotNull();
        assertThat(caughtParams.getParameterNames()).containsExactlyInAnyOrder(COLUMN_IP);
        assertThat(caughtParams.getValue(COLUMN_IP)).isEqualTo(IP);
    }

    @Test
    public void testLocationExistsFalse() throws Exception {
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        assertThat(repository.locationExists(IP)).isFalse();

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), captor.capture());
        verify(rsMock, times(1)).next();
        verifyNoMoreInteractions(parameterJdbcOperationsMock, rsMock);
        verifyNoInteractions(locationInsertMock, locationAccessInsertMock);

        MapSqlParameterSource caughtParams = captor.getValue();
        assertThat(caughtParams).isNotNull();
        assertThat(caughtParams.getParameterNames()).containsExactlyInAnyOrder(COLUMN_IP);
        assertThat(caughtParams.getValue(COLUMN_IP)).isEqualTo(IP);
    }

    @Test
    public void testLocationExistsWhenDataAccessException() throws Exception {
        DataAccessException dae = new InvalidDataAccessResourceUsageException("");
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenThrow(dae);

        assertThatThrownBy(() -> repository.locationExists(IP))
                .isInstanceOf(Exception.class)
                .hasCause(dae);

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), any(SqlParameterSource.class));
        verifyNoMoreInteractions(parameterJdbcOperationsMock);
        verifyNoInteractions(locationInsertMock, rsMock, locationAccessInsertMock);
    }

    @Test
    public void testSaveLocation() throws Exception {
        when(locationInsertMock.execute(any(SqlParameterSource.class))).thenReturn(1);

        LocationResponse location = new LocationResponse();
        location.setContinent("continent");
        location.setCountry("country");
        location.setCity("city");
        location.setLongitude(1.1);
        location.setLatitude(2.2);

        repository.saveLocation(IP, location);

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(locationInsertMock, times(1)).execute(captor.capture());
        verifyNoMoreInteractions(locationInsertMock);
        verifyNoInteractions(parameterJdbcOperationsMock, rsMock, locationAccessInsertMock);

        MapSqlParameterSource caughtParams = captor.getValue();
        assertThat(caughtParams).isNotNull();
        assertThat(caughtParams.getParameterNames()).containsExactlyInAnyOrder(COLUMN_IP, COLUMN_CONTINENT, COLUMN_COUNTRY, COLUMN_CITY, COLUMN_LONGITUDE, COLUMN_LATITUDE);
        assertThat(caughtParams.getValue(COLUMN_IP)).isEqualTo(IP);
        assertThat(caughtParams.getValue(COLUMN_CONTINENT)).isEqualTo(location.getContinent());
        assertThat(caughtParams.getValue(COLUMN_COUNTRY)).isEqualTo(location.getCountry());
        assertThat(caughtParams.getValue(COLUMN_CITY)).isEqualTo(location.getCity());
        assertThat(caughtParams.getValue(COLUMN_LONGITUDE)).isEqualTo(location.getLongitude());
        assertThat(caughtParams.getValue(COLUMN_LATITUDE)).isEqualTo(location.getLatitude());
    }

    @Test
    public void testSaveLocationWhenDataAccessException() {
        DataAccessException dae = new InvalidDataAccessResourceUsageException("");
        when(locationInsertMock.execute(any(SqlParameterSource.class))).thenThrow(dae);

        LocationResponse location = new LocationResponse();
        location.setContinent("continent");
        location.setCountry("country");
        location.setCity("city");
        location.setLongitude(1.1);
        location.setLatitude(2.2);

        assertThatThrownBy(() -> repository.saveLocation(IP, location))
                .isInstanceOf(Exception.class)
                .hasCause(dae);

        verify(locationInsertMock, times(1)).execute(any(SqlParameterSource.class));
        verifyNoMoreInteractions(locationInsertMock);
        verifyNoInteractions(parameterJdbcOperationsMock, rsMock, locationAccessInsertMock);
    }

    @Test
    public void testAuditLogAccess() throws Exception {
        when(locationAccessInsertMock.execute(any(SqlParameterSource.class))).thenReturn(1);

        repository.auditLogAccess(IP);

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(locationAccessInsertMock, times(1)).execute(captor.capture());
        verifyNoMoreInteractions(locationAccessInsertMock);
        verifyNoInteractions(parameterJdbcOperationsMock, rsMock, locationInsertMock);

        MapSqlParameterSource caughtParams = captor.getValue();
        assertThat(caughtParams).isNotNull();
        assertThat(caughtParams.getParameterNames()).containsExactlyInAnyOrder(DBSchema.GeolocationAccessTable.COLUMN_IP, COLUMN_TIMESTAMP);
        assertThat(caughtParams.getValue(DBSchema.GeolocationAccessTable.COLUMN_IP)).isEqualTo(IP);
    }

    @Test
    public void testAuditLogAccessWhenDataAccessException() {
        DataAccessException dae = new InvalidDataAccessResourceUsageException("");
        when(locationAccessInsertMock.execute(any(SqlParameterSource.class))).thenThrow(dae);

        assertThatThrownBy(() -> repository.auditLogAccess(IP))
                .isInstanceOf(Exception.class)
                .hasCause(dae);

        verify(locationAccessInsertMock, times(1)).execute(any(SqlParameterSource.class));
        verifyNoMoreInteractions(locationAccessInsertMock);
        verifyNoInteractions(parameterJdbcOperationsMock, rsMock, locationInsertMock);
    }
}
