package edu.weather.service.location.ipstack;

import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.Error;
import edu.weather.service.location.ipstack.model.LocationResponse;
import edu.weather.service.location.model.ILocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class LocationDetectionServiceImplTest {

    private static final String DUMMY_IP = "1.1.1.1";

    private static LocationDetectionServiceImpl service;

    private static RestOperations restMock;
    private static LocationResponse dummyLocation;
    private static LocationResponse errorLocation;

    @BeforeAll
    public static void beforeAll() {
        restMock = mock(RestOperations.class);
        service = new LocationDetectionServiceImpl(restMock);
        ReflectionTestUtils.setField(service, "apiKey", "dummyKey");
        ReflectionTestUtils.setField(service, "host", "dummyHost");

        dummyLocation = new LocationResponse();
        dummyLocation.setCity("City");

        Error error = new Error();
        error.setInfo("BOOM");
        errorLocation = new LocationResponse();
        errorLocation.setSuccess(false);
        errorLocation.setError(error);
    }

    @BeforeEach
    public void beforeEach() {
        reset(restMock);
    }

    @Test
    public void testResolveLocation() throws LocationDetectionException {
        when(restMock.getForObject(any(), eq(LocationResponse.class))).thenReturn(dummyLocation);

        ILocation location = service.resolveLocation(DUMMY_IP);

        assertThat(location).isNotNull().isEqualTo(dummyLocation);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restMock, times(1)).getForObject(captor.capture(), eq(LocationResponse.class));
        verifyNoMoreInteractions(restMock);

        URI uri = captor.getValue();
        assertThat(uri.getPath()).isNotBlank();
        assertThat(uri.getHost()).isEqualTo("dummyHost");
        assertThat(uri.getPath().substring(1)).isEqualTo(DUMMY_IP);
        assertThat(uri.getQuery()).contains("access_key=dummyKey");
    }

    @Test
    public void testResolveLocationWhenNullResponse() throws LocationDetectionException {
        assertThatThrownBy(() -> service.resolveLocation(DUMMY_IP))
                .isInstanceOf(LocationDetectionException.class)
                .hasMessage(null);

        verify(restMock, times(1)).getForObject(any(), eq(LocationResponse.class));
        verifyNoMoreInteractions(restMock);
    }

    @Test
    public void testResolveLocationWhenErrorResponse() {
        when(restMock.getForObject(any(), eq(LocationResponse.class))).thenReturn(errorLocation);

        assertThatThrownBy(() -> service.resolveLocation(DUMMY_IP))
                .isInstanceOf(LocationDetectionException.class)
                .hasMessage(errorLocation.getError().getInfo());

        verify(restMock, times(1)).getForObject(any(), eq(LocationResponse.class));
        verifyNoMoreInteractions(restMock);
    }

    @Test
    public void testResolveLocationWhenHttpClientError() {
        when(restMock.getForObject(any(), eq(LocationResponse.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "BOOM"));

        assertThatThrownBy(() -> service.resolveLocation(DUMMY_IP))
                .isInstanceOf(LocationDetectionException.class);

        verify(restMock, times(1)).getForObject(any(), eq(LocationResponse.class));
        verifyNoMoreInteractions(restMock);
    }

    @Test
    public void testResolveLocationWhenEmptyResponse() {
        when(restMock.getForObject(any(), eq(LocationResponse.class))).thenReturn(new LocationResponse());

        assertThatThrownBy(() -> service.resolveLocation(DUMMY_IP))
                .isInstanceOf(LocationDetectionException.class)
                .hasMessage("Cannot resolve location for IP address");

        verify(restMock, times(1)).getForObject(any(), eq(LocationResponse.class));
        verifyNoMoreInteractions(restMock);
    }
}
