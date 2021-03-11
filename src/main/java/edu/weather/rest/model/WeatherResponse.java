package edu.weather.rest.model;

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
public class WeatherResponse {

    private final String ip;
    private LocationDTO location;
    private WeatherDTO weather;
}
