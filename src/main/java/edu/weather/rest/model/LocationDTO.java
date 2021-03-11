package edu.weather.rest.model;

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
public class LocationDTO {

    private String continent;
    private String country;
    private String city;
    private Double latitude;
    private Double Longitude;
}
