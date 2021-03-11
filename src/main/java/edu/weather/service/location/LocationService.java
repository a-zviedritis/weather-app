package edu.weather.service.location;

import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.Location;

/**
 * @author andris
 * @since 1.0.0
 */
public interface LocationService {

    /**
     * Resolve the location information based on the provided IP address.
     *
     * @param ip IP address
     * @return {@link Location}
     * @throws LocationDetectionException in the event of failed integration
     */
    Location resolveLocation(String ip) throws LocationDetectionException;
}
