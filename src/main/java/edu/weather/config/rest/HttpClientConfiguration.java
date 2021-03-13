package edu.weather.config.rest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author andris
 * @since 1.0.0
 */
@Configuration
public class HttpClientConfiguration {

    // Determines the timeout in milliseconds until a connection is established.
    private static final int CONNECT_TIMEOUT = 10000;

    // The timeout when requesting a connection from the connection manager.
    private static final int REQUEST_TIMEOUT = 10000;

    // The timeout for waiting for data
    private static final int SOCKET_TIMEOUT = 10000;

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

}
