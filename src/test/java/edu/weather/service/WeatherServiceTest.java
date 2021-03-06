package edu.weather.service;

import edu.weather.repository.location.LocationRepository;
import edu.weather.repository.weather.WeatherConditionsRepository;
import edu.weather.service.location.LocationDetectionService;
import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.LocationResponse;
import edu.weather.service.location.model.ILocation;
import edu.weather.service.weather.WeatherDetectionService;
import edu.weather.service.weather.exception.LocationNotFoundException;
import edu.weather.service.weather.exception.WeatherDetectionException;
import edu.weather.service.weather.model.IWeatherInfo;
import edu.weather.service.weather.weatherapi.model.WeatherInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class WeatherServiceTest {

    private static final String DUMMY_IP = "1.1.1.1";
    private static final double LATITUDE = 12.34;
    private static final double LONGITUDE = 56.78;

    private static WeatherService service;

    private static LocationDetectionService locationServiceMock;
    private static WeatherDetectionService weatherServiceMock;
    private static LocationRepository locationRepositoryMock;
    private static WeatherConditionsRepository weatherConditionsRepositoryMock;
    private static Duration weatherConditionSaveInterval = Duration.ofMinutes(1);

    private ILocation location;
    private IWeatherInfo weatherInfo;

    @BeforeAll
    public static void beforeAll() {
        locationServiceMock = mock(LocationDetectionService.class);
        weatherServiceMock = mock(WeatherDetectionService.class);
        locationRepositoryMock = mock(LocationRepository.class);
        weatherConditionsRepositoryMock = mock(WeatherConditionsRepository.class);

        service = new WeatherService(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
        ReflectionTestUtils.setField(service, "weatherConditionSaveInterval", weatherConditionSaveInterval);
    }

    @BeforeEach
    public void beforeEach() {
        reset(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);

        LocationResponse l = new LocationResponse();
        l.setLatitude(LATITUDE);
        l.setLongitude(LONGITUDE);
        location = l;
        weatherInfo = new WeatherInfo();
    }

    @Test
    public void testDetectWeatherAndLocationExistsAndLastSavedWeatherConditionsExpired() throws Exception {
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());
        when(weatherConditionsRepositoryMock.getLastReportedConditionTimestamp(any(), any())).thenReturn(Instant.now().minus(weatherConditionSaveInterval));
        doNothing().when(weatherConditionsRepositoryMock).saveWeatherConditions(any(), any(), any());

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verify(weatherConditionsRepositoryMock, times(1)).getLastReportedConditionTimestamp(location.getLatitude(), location.getLongitude());
        verify(weatherConditionsRepositoryMock, times(1)).saveWeatherConditions(location.getLatitude(), location.getLongitude(), weatherInfo);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherAndLocationExistsAndLastSavedWeatherConditionsValid() throws Exception {
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());
        when(weatherConditionsRepositoryMock.getLastReportedConditionTimestamp(any(), any())).thenReturn(Instant.now());

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verify(weatherConditionsRepositoryMock, times(1)).getLastReportedConditionTimestamp(location.getLatitude(), location.getLongitude());
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherAndLocationDoesNotExist() throws Exception {
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(false);
        doNothing().when(locationRepositoryMock).saveLocation(anyString(), any());
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());
        when(weatherConditionsRepositoryMock.getLastReportedConditionTimestamp(any(), any())).thenReturn(null);
        doNothing().when(weatherConditionsRepositoryMock).saveWeatherConditions(any(), any(), any());

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).saveLocation(DUMMY_IP, location);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verify(weatherConditionsRepositoryMock, times(1)).getLastReportedConditionTimestamp(location.getLatitude(), location.getLongitude());
        verify(weatherConditionsRepositoryMock, times(1)).saveWeatherConditions(location.getLatitude(), location.getLongitude(), weatherInfo);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenGeolocationFails() throws LocationDetectionException {
        Throwable cause = new LocationDetectionException();
        when(locationServiceMock.resolveLocation(anyString())).thenThrow(cause);

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verifyNoMoreInteractions(locationServiceMock);
        verifyNoInteractions(weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenLocationExistenceCheckFails() throws Exception {
        Exception exception = new Exception("JDBC BOOM");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenThrow(exception);

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenLocationSavingFails() throws Exception {
        Exception exception = new Exception("JDBC BOOM");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(false);
        doThrow(exception).when(locationRepositoryMock).saveLocation(anyString(), any());

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).saveLocation(DUMMY_IP, location);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
    }

    @Test
    public void testDetectWeatherAndLocationAccessAuditLogFails() throws Exception {
        Exception exception = new Exception("JDBC BOOM");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(false);
        doNothing().when(locationRepositoryMock).saveLocation(anyString(), any());
        doThrow(exception).when(locationRepositoryMock).auditLogAccess(anyString());

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).saveLocation(DUMMY_IP, location);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenLocationCannotBeResolved() throws Exception {
        Throwable cause = new LocationNotFoundException("");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenThrow(cause);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
        verifyNoInteractions(weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenWeatherDetectionFails() throws Exception {
        Throwable cause = new WeatherDetectionException();
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenThrow(cause);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
        verifyNoInteractions(weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenWeatherConditionsLastTimestampCheckFails() throws Exception {
        Exception exception = new Exception("JDBC BOOM");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());
        when(weatherConditionsRepositoryMock.getLastReportedConditionTimestamp(any(), any())).thenThrow(exception);

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verify(weatherConditionsRepositoryMock, times(1)).getLastReportedConditionTimestamp(location.getLatitude(), location.getLongitude());
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenWeatherConditionsSaveFails() throws Exception {
        Exception exception = new Exception("JDBC BOOM");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);
        doNothing().when(locationRepositoryMock).auditLogAccess(anyString());
        when(weatherConditionsRepositoryMock.getLastReportedConditionTimestamp(any(), any())).thenReturn(null);
        doThrow(exception).when(weatherConditionsRepositoryMock).saveWeatherConditions(any(), any(), any());

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verify(locationRepositoryMock, times(1)).auditLogAccess(DUMMY_IP);
        verify(weatherConditionsRepositoryMock, times(1)).getLastReportedConditionTimestamp(location.getLatitude(), location.getLongitude());
        verify(weatherConditionsRepositoryMock, times(1)).saveWeatherConditions(location.getLatitude(), location.getLongitude(), weatherInfo);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock, weatherConditionsRepositoryMock);
    }
}
