package edu.weather.repository.weather;

import edu.weather.service.weather.model.IWeatherInfo;

import java.time.Instant;

/**
 * Weather conditions repository interface.
 *
 * @author andris
 * @since 1.0.0
 */
public interface WeatherConditionsRepository {

    /**
     * Retrieve the timestamp of previous reported weather conditions for the given coordinates.
     *
     * @param latitude Latitude
     * @param longitude Longitude
     * @return {@link Instant} of last reported timestamp, may be null
     */
    Instant getLastReportedConditionTimestamp(Double latitude, Double longitude) throws Exception;

    /**
     * Save the provided weather conditions for specific coordinates.
     *
     * @param latitude Latitude
     * @param longitude Longitude
     * @param weatherInfo Weather conditions
     */
    void saveWeatherConditions(Double latitude, Double longitude, IWeatherInfo weatherInfo) throws Exception;
}
