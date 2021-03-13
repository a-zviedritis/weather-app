package edu.weather.config.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpClientConfigurationTest {

    @Test
    public void testCloseableHttpClient() {
        HttpClientConfiguration config = new HttpClientConfiguration();

        CloseableHttpClient client = config.closeableHttpClient();

        assertThat(client).isNotNull();
    }
}
