package edu.weather.service;

import edu.weather.repository.location.LocationRepository;
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
import org.springframework.web.server.ResponseStatusException;

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

    private ILocation location;
    private IWeatherInfo weatherInfo;

    @BeforeAll
    public static void beforeAll() {
        locationServiceMock = mock(LocationDetectionService.class);
        weatherServiceMock = mock(WeatherDetectionService.class);
        locationRepositoryMock = mock(LocationRepository.class);

        service = new WeatherService(locationServiceMock, weatherServiceMock, locationRepositoryMock);
    }

    @BeforeEach
    public void beforeEach() {
        reset(locationServiceMock, weatherServiceMock, locationRepositoryMock);

        LocationResponse l = new LocationResponse();
        l.setLatitude(LATITUDE);
        l.setLongitude(LONGITUDE);
        location = l;
        weatherInfo = new WeatherInfo();
    }

    @Test
    public void testDetectWeatherAndLocationExists() throws Exception {
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);

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
    public void testDetectWeatherAndLocationDoesNotExist() throws Exception {
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(false);
        doNothing().when(locationRepositoryMock).saveLocation(anyString(), any());

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
    public void testDetectWeatherWhenGeolocationFails() throws LocationDetectionException {
        Throwable cause = new LocationDetectionException();
        when(locationServiceMock.resolveLocation(anyString())).thenThrow(cause);

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verifyNoMoreInteractions(locationServiceMock);
        verifyNoInteractions(weatherServiceMock, locationRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenLocationCannotBeResolved() throws Exception {
        Throwable cause = new LocationNotFoundException("");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenThrow(cause);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
    }

    @Test
    public void testDetectWeatherWhenWeatherDetectionFails() throws Exception {
        Throwable cause = new WeatherDetectionException();
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenThrow(cause);
        when(locationRepositoryMock.locationExists(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verify(locationRepositoryMock, times(1)).locationExists(DUMMY_IP);
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock, locationRepositoryMock);
    }
}
