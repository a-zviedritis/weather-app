package edu.weather.service.weather.weatherapi;

import edu.weather.service.weather.WeatherDetectionService;
import edu.weather.service.weather.exception.LocationNotFoundException;
import edu.weather.service.weather.exception.WeatherDetectionException;
import edu.weather.service.weather.model.IWeatherInfo;
import edu.weather.service.weather.weatherapi.model.Error;
import edu.weather.service.weather.weatherapi.model.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.StringJoiner;

/**
 * WeatherAPI specific {@link WeatherDetectionService} implementation.
 *
 * @author andris
 * @since 1.0.0
 */
@Service
@Qualifier("weatherapi")
@ConditionalOnProperty(name = "weather.provider", havingValue = "weatherapi", matchIfMissing = false)
public class WeatherDetectionServiceImpl implements WeatherDetectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherDetectionServiceImpl.class);

    private final RestOperations restOperations;

    @Value("${weather.weatherapi.apiKey}")
    private String apiKey;

    @Value("${weather.weatherapi.host}")
    private String host;

    @Autowired
    public WeatherDetectionServiceImpl(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public IWeatherInfo resolveWeatherInfo(double latitude, double longitude) throws LocationNotFoundException, WeatherDetectionException {
        String coordinates = new StringJoiner(",").add(String.valueOf(latitude)).add(String.valueOf(longitude)).toString();
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .path("v1/current.json")
                .queryParam("key", apiKey)
                .queryParam("q", coordinates)
                .build();

        WeatherResponse response;
        try {
            response = restOperations.getForObject(uri.toUri(), WeatherResponse.class);
        } catch (HttpClientErrorException clientError) {
            LOGGER.error(clientError.getMessage());
            throw new WeatherDetectionException(clientError);
        }

        if (response == null) {
            LOGGER.error("null received during weather detection");
            throw new WeatherDetectionException();
        } else if (response.getError() != null) {
            Error error = response.getError();
            LOGGER.error("Error received during location detection: {}", error.toString());
            if (Error.Code.LOCATION_NOT_FOUND.getCode() == error.getCode()) {
                throw new LocationNotFoundException(String.format("Unable to determine location for coordinates %s", coordinates));
            } else {
                throw new WeatherDetectionException(error.getMessage());
            }
        }

        return response.getCurrent();
    }
}
