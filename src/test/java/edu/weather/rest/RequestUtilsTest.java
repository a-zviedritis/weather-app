package edu.weather.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class RequestUtilsTest {

    private static final String DUMMY_IP = "123.123.123.123";

    @Mock
    HttpServletRequest requestMock;

    @Test
    public void testResolveClientIPFromHeader() {
        when(requestMock.getHeader(anyString())).thenReturn(DUMMY_IP);

        String clientIP = RequestUtils.resolveClientIP(requestMock);

        assertThat(clientIP)
                .isNotBlank()
                .isEqualTo(DUMMY_IP);
    }

    @Test
    public void testResolveClientIPWhenHeaderHasMultipleValues() {
        when(requestMock.getHeader(anyString())).thenReturn(String.format("%s,%s", DUMMY_IP, "234.234.234.234"));

        String clientIP = RequestUtils.resolveClientIP(requestMock);

        assertThat(clientIP)
                .isNotBlank()
                .isEqualTo(DUMMY_IP);
    }

    @Test
    public void testResolveClientIPWhenNoHeader() {
        when(requestMock.getRemoteAddr()).thenReturn(DUMMY_IP);

        String clientIP = RequestUtils.resolveClientIP(requestMock);

        assertThat(clientIP)
                .isNotBlank()
                .isEqualTo(DUMMY_IP);
    }

}
