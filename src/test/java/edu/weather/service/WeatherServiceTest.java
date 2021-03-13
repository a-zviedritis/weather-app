package edu.weather.service;

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

    private ILocation location;
    private IWeatherInfo weatherInfo;

    @BeforeAll
    public static void beforeAll() {
        locationServiceMock = mock(LocationDetectionService.class);
        weatherServiceMock = mock(WeatherDetectionService.class);

        service = new WeatherService(locationServiceMock, weatherServiceMock);
    }

    @BeforeEach
    public void beforeEach() {
        reset(locationServiceMock, weatherServiceMock);

        LocationResponse l = new LocationResponse();
        l.setLatitude(LATITUDE);
        l.setLongitude(LONGITUDE);
        location = l;
        weatherInfo = new WeatherInfo();
    }

    @Test
    public void testDetectWeather() throws LocationDetectionException, WeatherDetectionException, LocationNotFoundException {
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenReturn(weatherInfo);

        Pair<ILocation, IWeatherInfo> response = service.detectWeather(DUMMY_IP);

        assertThat(response).isNotNull();
        assertThat(response.getKey()).isEqualTo(location);
        assertThat(response.getValue()).isEqualTo(weatherInfo);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock);
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
        verifyNoInteractions(weatherServiceMock);
    }

    @Test
    public void testDetectWeatherWhenLocationCannotBeResolved() throws LocationDetectionException, WeatherDetectionException, LocationNotFoundException {
        Throwable cause = new LocationNotFoundException("");
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenThrow(cause);

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock);
    }

    @Test
    public void testDetectWeatherWhenWeatherDetectionFails() throws LocationDetectionException, WeatherDetectionException, LocationNotFoundException {
        Throwable cause = new WeatherDetectionException();
        when(locationServiceMock.resolveLocation(anyString())).thenReturn(location);
        when(weatherServiceMock.resolveWeatherInfo(any(Double.class), any(Double.class))).thenThrow(cause);

        assertThatThrownBy(() -> service.detectWeather(DUMMY_IP))
                .isInstanceOf(ResponseStatusException.class)
                .hasCause(cause);

        verify(locationServiceMock, times(1)).resolveLocation(eq(DUMMY_IP));
        verify(weatherServiceMock, times(1)).resolveWeatherInfo(eq(location.getLatitude()), eq(location.getLongitude()));
        verifyNoMoreInteractions(locationServiceMock, weatherServiceMock);
    }
}
