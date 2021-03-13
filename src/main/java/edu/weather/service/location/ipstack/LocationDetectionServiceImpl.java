package edu.weather.service.location.ipstack;

import edu.weather.service.location.LocationDetectionService;
import edu.weather.service.location.cache.LocationCacheConfiguration;
import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.LocationResponse;
import edu.weather.service.location.model.ILocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Ipstack specific {@link LocationDetectionService} implementation.
 *
 * @author andris
 * @since 1.0.0
 */
@Service
@Qualifier("ipstack")
@ConditionalOnProperty(name = "location.provider", havingValue = "ipstack", matchIfMissing = false)
public class LocationDetectionServiceImpl implements LocationDetectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDetectionServiceImpl.class);

    private final RestOperations restOperations;

    @Value("${location.ipstack.apiKey}")
    private String apiKey;

    @Value("${location.ipstack.host}")
    private String host;

    @Autowired
    public LocationDetectionServiceImpl(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    @Cacheable(LocationCacheConfiguration.CACHE_NAME)
    public ILocation resolveLocation(String ip) throws LocationDetectionException {
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .path(ip)
                .queryParam("access_key", apiKey)
                .build();

        LocationResponse response;
        try {
            response = restOperations.getForObject(uri.toUri(), LocationResponse.class);
        } catch (HttpClientErrorException clientError) {
            LOGGER.error(clientError.getMessage());
            throw new LocationDetectionException(clientError);
        }

        if (response == null) {
            LOGGER.error("null received during location detection");
            throw new LocationDetectionException();
        } else if (!response.isSuccess()) {
            LOGGER.error("Error received during location detection: {}", response.getError().toString());
            throw new LocationDetectionException(response.getError().getInfo());
        }

        return response;
    }
}
