package edu.weather.service.location.ipstack.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Location {

    @JsonProperty("continent_name")
    private String continent;

    @JsonProperty("country_name")
    private String country;
    private String city;
    private Double latitude;
    private Double Longitude;

    private boolean success = true; // ipstack returns false in case of an error

    private Error error;
}
