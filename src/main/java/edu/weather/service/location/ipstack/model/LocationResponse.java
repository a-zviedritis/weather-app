package edu.weather.service.location.ipstack.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.weather.service.location.model.ILocation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author andris
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class LocationResponse implements ILocation, Serializable {

    private static final int serialVersionUID = -1;

    @JsonProperty("continent_name")
    private String continent;

    @JsonProperty("country_name")
    private String country;
    private String city;
    private Double latitude;
    private Double longitude;

    private boolean success = true; // ipstack returns false in case of an error

    private Error error;

    public boolean isEmpty() {
        return StringUtils.isEmpty(continent) &&
                StringUtils.isEmpty(country) &&
                StringUtils.isEmpty(city) &&
                latitude == null &&
                longitude == null;
    }
}
