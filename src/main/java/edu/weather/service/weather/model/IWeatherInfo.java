package edu.weather.service.weather.model;

/**
 * @author andris
 * @since 1.0.0
 */
public interface IWeatherInfo {

    Double getTemperature();
    Double getTemperatureFeelsLike();
    String getCondition();
    Integer getHumidity();
    Double getWindSpeed();
    Double getGustSpeed();
    String getWindDirection();
    Integer getWindDegree();
}
