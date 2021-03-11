package edu.weather.service.location.ipstack;

import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.Error;
import edu.weather.service.location.ipstack.model.Location;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestOperations;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class LocationServiceImplTest {

    private static final String DUMMY_IP = "1.1.1.1";

    private static LocationServiceImpl service;

    private static RestOperations restMock;
    private static Location dummyLocation;
    private static Location errorLocation;

    @BeforeAll
    public static void beforeAll() {
        restMock = mock(RestOperations.class);
        service = new LocationServiceImpl(restMock);
        ReflectionTestUtils.setField(service, "apiKey", "dummyKey");
        ReflectionTestUtils.setField(service, "host", "dummyHost");

        dummyLocation = new Location();
        dummyLocation.setCity("City");

        Error error = new Error();
        error.setInfo("BOOM");
        errorLocation = new Location();
        errorLocation.setSuccess(false);
        errorLocation.setError(error);
    }

    @BeforeEach
    public void beforeEach() {
        reset(restMock);
    }

    @Test
    public void testResolveLocation() throws LocationDetectionException {
        when(restMock.getForObject(any(), eq(Location.class))).thenReturn(dummyLocation);

        Location location = service.resolveLocation(DUMMY_IP);

        assertThat(location).isNotNull().isEqualTo(dummyLocation);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restMock, times(1)).getForObject(captor.capture(), eq(Location.class));

        URI uri = captor.getValue();
        assertThat(uri.getPath()).isNotBlank();
        assertThat(uri.getPath().substring(1)).isEqualTo(DUMMY_IP);
    }

    @Test
    public void testResolveLocationWhenNullResponse() throws LocationDetectionException {
        assertThatThrownBy(() -> service.resolveLocation(DUMMY_IP))
                .isInstanceOf(LocationDetectionException.class)
                .hasMessage(null);

        verify(restMock, times(1)).getForObject(any(), eq(Location.class));
    }

    @Test
    public void testResolveLocationWhenErrorResponse() {
        when(restMock.getForObject(any(), eq(Location.class))).thenReturn(errorLocation);

        assertThatThrownBy(() -> service.resolveLocation(DUMMY_IP))
                .isInstanceOf(LocationDetectionException.class)
                .hasMessage(errorLocation.getError().getInfo());

        verify(restMock, times(1)).getForObject(any(), eq(Location.class));
    }
}
