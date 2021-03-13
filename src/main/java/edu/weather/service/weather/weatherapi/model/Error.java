package edu.weather.service.weather.weatherapi.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author andris
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Error {

    private Integer code;
    private String message;

    @Getter
    public enum Code {
        LOCATION_NOT_FOUND(1006);

        private int code;

        Code(int code) {
            this.code = code;
        }
    }
}
