package edu.weather.service.location.ipstack;

import edu.weather.service.location.LocationService;
import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Ipstack specific {@link LocationService} implementation.
 *
 * @author andris
 * @since 1.0.0
 */
@Service
@Qualifier("ipstack")
@ConditionalOnProperty(name = "location.provider", havingValue = "ipstack", matchIfMissing = false)
public class LocationServiceImpl implements LocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final RestOperations restOperations;

    @Value("${location.ipstack.apiKey}")
    private String apiKey;

    @Value("${location.ipstack.host}")
    private String host;

    @Autowired
    public LocationServiceImpl(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public Location resolveLocation(String ip) throws LocationDetectionException {
        UriComponents uri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(host)
                .path(ip)
                .queryParam("access_key", apiKey)
                .build();
        Location location = restOperations.getForObject(uri.toUri(), Location.class);

        if (location == null) {
            LOGGER.error("null received during location detection");
            throw new LocationDetectionException();
        } else if (!location.isSuccess()) {
            LOGGER.error("Error received during location detection {}", location.getError().toString());
            throw new LocationDetectionException(location.getError().getInfo());
        }

        return location;
    }
}
