package edu.weather.service.weather.weatherapi;

import edu.weather.service.weather.WeatherDetectionService;
import edu.weather.service.weather.exception.LocationNotFoundException;
import edu.weather.service.weather.exception.WeatherDetectionException;
import edu.weather.service.weather.model.IWeatherInfo;
import edu.weather.service.weather.weatherapi.model.Error;
import edu.weather.service.weather.weatherapi.model.WeatherInfo;
import edu.weather.service.weather.weatherapi.model.WeatherResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class WeatherDetectionServiceImplTest {

    private static final double LATITUDE = 12.34;
    private static final double LONGITUDE = 34.56;

    private static WeatherDetectionService service;

    private static RestOperations restMock;
    private static WeatherResponse dummyResponse;
    private WeatherResponse errorResponse;

    @BeforeAll
    public static void beforeAll() {
        restMock = mock(RestOperations.class);
        service = new WeatherDetectionServiceImpl(restMock);
        ReflectionTestUtils.setField(service, "apiKey", "dummyKey");
        ReflectionTestUtils.setField(service, "host", "dummyHost");

        WeatherInfo info = new WeatherInfo();
        info.setTemperature(1.2);
        dummyResponse = new WeatherResponse();
        dummyResponse.setCurrent(info);
    }

    @BeforeEach
    public void beforeEach() {
        reset(restMock);

        errorResponse = new WeatherResponse();
    }

    @Test
    public void testResolveWeatherInfo() throws WeatherDetectionException, LocationNotFoundException {
        when(restMock.getForObject(any(), eq(WeatherResponse.class))).thenReturn(dummyResponse);

        IWeatherInfo weather = service.resolveWeatherInfo(LATITUDE, LONGITUDE);

        assertThat(weather).isNotNull().isEqualTo(dummyResponse.getCurrent());

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restMock, times(1)).getForObject(captor.capture(), eq(WeatherResponse.class));
        verifyNoMoreInteractions(restMock);

        URI uri = captor.getValue();
        assertThat(uri.getPath()).isNotBlank();
        assertThat(uri.getHost()).isEqualTo("dummyHost");
        assertThat(uri.getPath().substring(1)).isEqualTo("v1/current.json");
        assertThat(uri.getQuery())
                .contains("key=dummyKey")
                .contains(String.format("q=%.2f,%.2f", LATITUDE, LONGITUDE));
    }

    @Test
    public void testResolveWeatherInfoWhenNullResponse() {
        assertThatThrownBy(() -> service.resolveWeatherInfo(LATITUDE, LONGITUDE))
                .isInstanceOf(WeatherDetectionException.class)
                .hasMessage(null);

        verify(restMock, times(1)).getForObject(any(), eq(WeatherResponse.class));
        verifyNoMoreInteractions(restMock);
    }

    @Test
    public void testResolveWeatherInfoWhenErrorResponseWithLocationNotFoundCode() {
        Error error = new Error();
        error.setCode(Error.Code.LOCATION_NOT_FOUND.getCode());
        errorResponse.setError(error);
        when(restMock.getForObject(any(), eq(WeatherResponse.class))).thenReturn(errorResponse);

        assertThatThrownBy(() -> service.resolveWeatherInfo(LATITUDE, LONGITUDE))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage(String.format("Unable to determine location for coordinates %.2f,%.2f", LATITUDE, LONGITUDE));

        verify(restMock, times(1)).getForObject(any(), eq(WeatherResponse.class));
        verifyNoMoreInteractions(restMock);
    }

    @Test
    public void testResolveWeatherInfoWhenErrorResponseWithUnknownCode() {
        Error error = new Error();
        error.setCode(1);
        error.setMessage("BOOM");
        errorResponse.setError(error);
        when(restMock.getForObject(any(), eq(WeatherResponse.class))).thenReturn(errorResponse);

        assertThatThrownBy(() -> service.resolveWeatherInfo(LATITUDE, LONGITUDE))
                .isInstanceOf(WeatherDetectionException.class)
                .hasMessage(error.getMessage());

        verify(restMock, times(1)).getForObject(any(), eq(WeatherResponse.class));
        verifyNoMoreInteractions(restMock);
    }

    @Test
    public void testResolveWeatherInfoWhenHttpClientError() {
        when(restMock.getForObject(any(), eq(WeatherResponse.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "BOOM"));

        assertThatThrownBy(() -> service.resolveWeatherInfo(LATITUDE, LONGITUDE))
                .isInstanceOf(WeatherDetectionException.class);

        verify(restMock, times(1)).getForObject(any(), eq(WeatherResponse.class));
        verifyNoMoreInteractions(restMock);
    }
}
