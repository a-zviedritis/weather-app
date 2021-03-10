package edu.weather.rest.model;

import edu.weather.model.Location;
import edu.weather.model.Weather;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author andris
 * @since 1.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class WeatherInfo {

    private final String ip;
    private Location location;
    private Weather weather;
}
