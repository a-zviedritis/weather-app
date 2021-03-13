package edu.weather.service.weather.weatherapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.weather.service.weather.model.IWeatherInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author andris
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class WeatherInfo implements IWeatherInfo, Serializable {

    private static final int serialVersionUID = -1;

    @JsonProperty("temp_c")
    private Double temperature;

    @JsonProperty("feelslike_c")
    private Double temperatureFeelsLike;

    private Condition condition;

    private Integer humidity;

    @JsonProperty("wind_kph")
    private Double windSpeed;

    @JsonProperty("gust_kph")
    private Double gustSpeed;

    @JsonProperty("wind_dir")
    private String windDirection;

    @JsonProperty("wind_degree")
    private Integer windDegree;

    @Override
    public String getCondition() {
        return condition.getText();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Condition implements Serializable {

        private static final int serialVersionUID = -1;

        private String text;
    }
}
