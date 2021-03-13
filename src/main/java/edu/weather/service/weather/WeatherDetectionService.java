package edu.weather.service.weather;

import edu.weather.service.weather.exception.LocationNotFoundException;
import edu.weather.service.weather.exception.WeatherDetectionException;
import edu.weather.service.weather.model.IWeatherInfo;

/**
 * @author andris
 * @since 1.0.0
 */
public interface WeatherDetectionService {

    /**
     * Resolve the current weather conditions for the provided coordinates.
     *
     * @param latitude Latitude
     * @param longitude Longitude
     * @return {@link IWeatherInfo}
     * @throws LocationNotFoundException when coordinates don't map to any location
     * @throws WeatherDetectionException in the event of failed integration
     */
    IWeatherInfo resolveWeatherInfo(double latitude, double longitude) throws LocationNotFoundException, WeatherDetectionException;
}
