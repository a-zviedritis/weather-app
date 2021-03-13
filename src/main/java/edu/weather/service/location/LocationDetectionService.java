package edu.weather.service.location;

import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.model.ILocation;

/**
 * @author andris
 * @since 1.0.0
 */
public interface LocationDetectionService {

    /**
     * Resolve the location information based on the provided IP address.
     *
     * @param ip IP address
     * @return {@link ILocation}
     * @throws LocationDetectionException in the event of failed integration
     */
    ILocation resolveLocation(String ip) throws LocationDetectionException;
}
