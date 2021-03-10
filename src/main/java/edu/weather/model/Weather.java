package edu.weather.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author andris
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
public class Weather {

    private Double temperature;
    private Double temperatureFeelsLike;
    private String condition;
    private Integer humidity;
    private Double windSpeed;
    private Double gustSpeed;
    private String windDirection;
    private Integer windDegree;
}
