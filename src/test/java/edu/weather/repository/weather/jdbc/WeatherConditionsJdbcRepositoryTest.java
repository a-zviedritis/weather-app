package edu.weather.repository.weather.jdbc;

import edu.weather.service.weather.weatherapi.model.WeatherInfo;
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
import java.sql.Timestamp;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class WeatherConditionsJdbcRepositoryTest {

    private static final Double LATITUDE = 1.1;
    private static final Double LONGITUDE = 2.2;

    private static WeatherConditionsJdbcRepository repository;
    private static SimpleJdbcInsertOperations weatherConditionInsertMock;
    private static NamedParameterJdbcOperations parameterJdbcOperationsMock;
    private static SqlRowSet rsMock;

    @BeforeAll
    public static void beforeAll() {
        weatherConditionInsertMock = mock(SimpleJdbcInsertOperations.class);
        parameterJdbcOperationsMock = mock(NamedParameterJdbcOperations.class);

        repository = new WeatherConditionsJdbcRepository(mock(DataSource.class));
        ReflectionTestUtils.setField(repository, "weatherConditionsInsert", weatherConditionInsertMock);
        ReflectionTestUtils.setField(repository, "parameterJdbcOperations", parameterJdbcOperationsMock);

        rsMock = mock(SqlRowSet.class);
    }

    @BeforeEach
    public void beforeEach() {
        reset(weatherConditionInsertMock, parameterJdbcOperationsMock, rsMock);
    }

    @Test
    public void testGetLastReportedConditionTimestampWhenNoResult() throws Exception {
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);

        Instant timestamp = repository.getLastReportedConditionTimestamp(LATITUDE, LONGITUDE);

        assertThat(timestamp).isNull();

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), any(SqlParameterSource.class));
        verify(rsMock, times(1)).next();
        verifyNoMoreInteractions(parameterJdbcOperationsMock, rsMock);
        verifyNoInteractions(weatherConditionInsertMock);
    }

    @Test
    public void testGetLastReportedConditionTimestampWhenNullResult() throws Exception {
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.wasNull()).thenReturn(true);

        Instant timestamp = repository.getLastReportedConditionTimestamp(LATITUDE, LONGITUDE);

        assertThat(timestamp).isNull();

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), any(SqlParameterSource.class));
        verify(rsMock, times(1)).next();
        verify(rsMock, times(1)).getTimestamp(anyString());
        verify(rsMock, times(1)).wasNull();
        verifyNoMoreInteractions(parameterJdbcOperationsMock, rsMock);
        verifyNoInteractions(weatherConditionInsertMock);
    }

    @Test
    public void testGetLastReportedConditionTimestamp() throws Exception {
        Instant lastTimestamp = Instant.now();
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getTimestamp(anyString())).thenReturn(Timestamp.from(lastTimestamp));
        when(rsMock.wasNull()).thenReturn(false);

        Instant timestamp = repository.getLastReportedConditionTimestamp(LATITUDE, LONGITUDE);

        assertThat(timestamp).isNotNull().isEqualTo(lastTimestamp);

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), captor.capture());
        verify(rsMock, times(1)).next();
        verify(rsMock, times(1)).getTimestamp(anyString());
        verify(rsMock, times(1)).wasNull();
        verifyNoMoreInteractions(parameterJdbcOperationsMock, rsMock);
        verifyNoInteractions(weatherConditionInsertMock);

        MapSqlParameterSource caughtParams = captor.getValue();
        assertThat(caughtParams).isNotNull();
        assertThat(caughtParams.getParameterNames()).containsExactlyInAnyOrder("latitude", "longitude");
        assertThat(caughtParams.getValue("latitude")).isEqualTo(LATITUDE);
        assertThat(caughtParams.getValue("longitude")).isEqualTo(LONGITUDE);
    }

    @Test
    public void testGetLastReportedConditionTimestampWhenDataAccessException() throws Exception {
        DataAccessException dae = new InvalidDataAccessResourceUsageException("");
        when(parameterJdbcOperationsMock.queryForRowSet(anyString(), any(SqlParameterSource.class))).thenThrow(dae);

        assertThatThrownBy(() -> repository.getLastReportedConditionTimestamp(LATITUDE, LONGITUDE))
                .isInstanceOf(Exception.class)
                .hasCause(dae);

        verify(parameterJdbcOperationsMock, times(1)).queryForRowSet(anyString(), any(SqlParameterSource.class));
        verifyNoMoreInteractions(parameterJdbcOperationsMock);
        verifyNoInteractions(weatherConditionInsertMock, rsMock);
    }

    @Test
    public void testSaveWeatherConditions() throws Exception {
        when(weatherConditionInsertMock.execute(any(SqlParameterSource.class))).thenReturn(1);

        WeatherInfo wi = new WeatherInfo();
        wi.setTemperature(1.1);
        wi.setHumidity(2);
        wi.setWindSpeed(3.3);
        wi.setGustSpeed(4.4);
        wi.setWindDirection("N");
        WeatherInfo.Condition condition = new WeatherInfo.Condition();
        condition.setText("Sup");
        wi.setCondition(condition);

        repository.saveWeatherConditions(LATITUDE, LONGITUDE, wi);

        ArgumentCaptor<MapSqlParameterSource> captor = ArgumentCaptor.forClass(MapSqlParameterSource.class);

        verify(weatherConditionInsertMock, times(1)).execute(captor.capture());
        verifyNoMoreInteractions(weatherConditionInsertMock);
        verifyNoInteractions(parameterJdbcOperationsMock);

        MapSqlParameterSource caughtParams = captor.getValue();
        assertThat(caughtParams).isNotNull();
        assertThat(caughtParams.getParameterNames()).containsExactlyInAnyOrder("latitude", "longitude", "condition", "temperature", "humidity", "wind_speed", "gust_speed", "wind_direction", "timestamp");
        assertThat(caughtParams.getValue("latitude")).isEqualTo(LATITUDE);
        assertThat(caughtParams.getValue("longitude")).isEqualTo(LONGITUDE);
        assertThat(caughtParams.getValue("condition")).isEqualTo(wi.getCondition());
        assertThat(caughtParams.getValue("temperature")).isEqualTo(wi.getTemperature());
        assertThat(caughtParams.getValue("humidity")).isEqualTo(wi.getHumidity());
        assertThat(caughtParams.getValue("wind_speed")).isEqualTo(wi.getWindSpeed());
        assertThat(caughtParams.getValue("gust_speed")).isEqualTo(wi.getGustSpeed());
        assertThat(caughtParams.getValue("wind_direction")).isEqualTo(wi.getWindDirection());
    }

    @Test
    public void testSaveWeatherConditionsWhenDataAccessException() {
        DataAccessException dae = new InvalidDataAccessResourceUsageException("");
        when(weatherConditionInsertMock.execute(any(SqlParameterSource.class))).thenThrow(dae);

        WeatherInfo wi = new WeatherInfo();
        wi.setTemperature(1.1);
        wi.setHumidity(2);
        wi.setWindSpeed(3.3);
        wi.setGustSpeed(4.4);
        wi.setWindDirection("N");
        WeatherInfo.Condition condition = new WeatherInfo.Condition();
        condition.setText("Sup");
        wi.setCondition(condition);


        assertThatThrownBy(() -> repository.saveWeatherConditions(LATITUDE, LONGITUDE, wi))
                .isInstanceOf(Exception.class)
                .hasCause(dae);

        verify(weatherConditionInsertMock, times(1)).execute(any(SqlParameterSource.class));
        verifyNoMoreInteractions(weatherConditionInsertMock);
        verifyNoInteractions(parameterJdbcOperationsMock);
    }
}
