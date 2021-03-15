package edu.weather.rest;

import edu.weather.rest.mapper.LocationMapper;
import edu.weather.rest.mapper.WeatherMapper;
import edu.weather.rest.model.LocationDTO;
import edu.weather.rest.model.WeatherDTO;
import edu.weather.rest.model.WeatherResponse;
import edu.weather.service.WeatherService;
import edu.weather.service.location.ipstack.model.LocationResponse;
import edu.weather.service.location.model.ILocation;
import edu.weather.service.weather.model.IWeatherInfo;
import edu.weather.service.weather.weatherapi.model.WeatherInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class WeatherControllerTest {

    private static final String IP_1 = "1.1.1.1";
    private static final String IP_2 = "2.2.2.2";

    private static WeatherController controller;

    private static WeatherService serviceMock;
    private static LocationMapper locationMapperMock;
    private static WeatherMapper weatherMapperMock;
    private static HttpServletRequest requestMock;

    private ILocation location;
    private LocationDTO locationDTO;
    private IWeatherInfo weatherInfo;
    private WeatherDTO weatherDTO;

    @BeforeAll
    public static void beforeAll() {
        serviceMock = mock(WeatherService.class);
        locationMapperMock = mock(LocationMapper.class);
        weatherMapperMock = mock(WeatherMapper.class);

        controller = new WeatherController(serviceMock, locationMapperMock, weatherMapperMock);

        requestMock = mock(HttpServletRequest.class);
    }

    @BeforeEach
    public void beforeEach() {
        reset(serviceMock, locationMapperMock, weatherMapperMock, requestMock);

        location = new LocationResponse();
        locationDTO = new LocationDTO();
        weatherInfo = new WeatherInfo();
        weatherDTO = new WeatherDTO();
    }

    @Test
    public void testDetectWeather() {
        when(serviceMock.detectWeather(anyString())).thenReturn(Pair.of(location, weatherInfo));
        when(locationMapperMock.toDTO(any())).thenReturn(locationDTO);
        when(weatherMapperMock.toDTO(any())).thenReturn(weatherDTO);
        when(requestMock.getRemoteAddr()).thenReturn(IP_1);

        WeatherResponse response = controller.detectWeather(null, requestMock);

        assertThat(response).isNotNull();
        assertThat(response.getIp()).isEqualTo(IP_1);
        assertThat(response.getLocation()).isEqualTo(locationDTO);
        assertThat(response.getWeather()).isEqualTo(weatherDTO);

        verify(serviceMock, times(1)).detectWeather(eq(IP_1));
        verify(locationMapperMock, times(1)).toDTO(eq(location));
        verify(weatherMapperMock, times(1)).toDTO(eq(weatherInfo));
        verifyNoMoreInteractions(serviceMock, locationMapperMock, weatherMapperMock);
    }

    @Test
    public void testDetectWeatherWithSpecificIPRequested() {
        when(serviceMock.detectWeather(anyString())).thenReturn(Pair.of(location, weatherInfo));
        when(locationMapperMock.toDTO(any())).thenReturn(locationDTO);
        when(weatherMapperMock.toDTO(any())).thenReturn(weatherDTO);
        when(requestMock.getRemoteAddr()).thenReturn(IP_1);

        WeatherResponse response = controller.detectWeather(IP_2, requestMock);

        assertThat(response).isNotNull();
        assertThat(response.getIp()).isEqualTo(IP_2);
        assertThat(response.getLocation()).isEqualTo(locationDTO);
        assertThat(response.getWeather()).isEqualTo(weatherDTO);

        verify(serviceMock, times(1)).detectWeather(eq(IP_2));
        verify(locationMapperMock, times(1)).toDTO(eq(location));
        verify(weatherMapperMock, times(1)).toDTO(eq(weatherInfo));
        verifyNoMoreInteractions(serviceMock, locationMapperMock, weatherMapperMock);
    }

    @Test
    public void testDetectWeatherWithIPv6Address() {
        assertThatThrownBy(() -> controller.detectWeather("0:0:0:0:0:0:0:1", requestMock))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid IPv4 address detected/provided");

        verifyNoInteractions(serviceMock, locationMapperMock, weatherMapperMock);
    }
}
