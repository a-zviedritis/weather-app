package edu.weather.config.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author andris
 * @since 1.0.0
 */
@Configuration
public class RestTemplateConfiguration {

    @Autowired
    private CloseableHttpClient httpClient;

    @Value("${restTemplate.connectTimeout:1000}")
    private int restTemplateConnectionTimeout;

    @Value("${restTemplate.readTimeout:3000}")
    private int restTemplateReadTimeout;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();

        clientHttpRequestFactory.setConnectTimeout(restTemplateConnectionTimeout);
        clientHttpRequestFactory.setReadTimeout(restTemplateReadTimeout);
        clientHttpRequestFactory.setHttpClient(httpClient);

        return clientHttpRequestFactory;
    }
}
