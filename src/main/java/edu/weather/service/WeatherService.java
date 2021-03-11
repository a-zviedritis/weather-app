package edu.weather.service;

import edu.weather.service.location.LocationService;
import edu.weather.service.location.exception.LocationDetectionException;
import edu.weather.service.location.ipstack.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author andris
 * @since 1.0.0
 */
@Service
public class WeatherService {

    private final LocationService locationService;

    @Autowired
    public WeatherService(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Detects the location based on the provided IP address.
     *
     * @param ip IP address
     * @return {@link Location}
     * @throws LocationDetectionException
     */
    public Location detectLocation(String ip) throws LocationDetectionException {
        return locationService.resolveLocation(ip);
    }
}
