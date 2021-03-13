package edu.weather.config.rest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RestTemplateConfigurationTest {

    @Test
    public void testClientHttpRequestFactory() {
        RestTemplateConfiguration config = new RestTemplateConfiguration();

        CloseableHttpClient clientMock = mock(CloseableHttpClient.class);

        ReflectionTestUtils.setField(config, "httpClient", clientMock);
        ReflectionTestUtils.setField(config, "restTemplateConnectionTimeout", 1);
        ReflectionTestUtils.setField(config, "restTemplateReadTimeout", 2);

        HttpComponentsClientHttpRequestFactory factory = config.clientHttpRequestFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getHttpClient()).isEqualTo(clientMock);

        RequestConfig requestConfig = (RequestConfig) ReflectionTestUtils.getField(factory, "requestConfig");
        assertThat(requestConfig.getConnectTimeout()).isEqualTo(1);
        assertThat(requestConfig.getSocketTimeout()).isEqualTo(2);
    }
}
