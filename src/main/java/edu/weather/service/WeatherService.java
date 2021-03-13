package edu.weather.service;

import edu.weather.service.location.LocationDetectionService;
import edu.weather.service.location.model.ILocation;
import edu.weather.service.weather.WeatherDetectionService;
import edu.weather.service.weather.exception.LocationNotFoundException;
import edu.weather.service.weather.model.IWeatherInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author andris
 * @since 1.0.0
 */
@Service
public class WeatherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherService.class);

    private final LocationDetectionService locationService;
    private final WeatherDetectionService weatherService;

    @Autowired
    public WeatherService(LocationDetectionService locationService,
                          WeatherDetectionService weatherService) {
        this.locationService = locationService;
        this.weatherService = weatherService;
    }

    /**
     * Detects the location and weather information based on the provided IP address.
     *
     * @param ip IP address
     * @return {@link Pair} of location and weather information
     */
    public Pair<ILocation, IWeatherInfo> detectWeather(String ip) {
        ILocation location = detectLocation(ip);
        IWeatherInfo weather = resolveWeatherInfo(location);

        return Pair.of(location, weather);
    }

    private ILocation detectLocation(String ip) {
        try {
            return locationService.resolveLocation(ip);
        } catch (Exception e) {
            LOGGER.error("Error during location detection: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to detect location info", e);
        }
    }

    private IWeatherInfo resolveWeatherInfo(ILocation location) {
        try {
            return weatherService.resolveWeatherInfo(location.getLatitude(), location.getLongitude());
        } catch (LocationNotFoundException lnfe) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, lnfe.getMessage(), lnfe);
        } catch (Exception e) {
            LOGGER.error("Error during weather detection: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to detect weather info", e);
        }
    }
}
