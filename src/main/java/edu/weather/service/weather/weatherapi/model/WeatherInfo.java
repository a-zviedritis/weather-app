package edu.weather.service.weather.weatherapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.weather.service.weather.model.IWeatherInfo;
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
public class WeatherInfo implements IWeatherInfo {

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
    public static class Condition {

        private String text;
    }
}
