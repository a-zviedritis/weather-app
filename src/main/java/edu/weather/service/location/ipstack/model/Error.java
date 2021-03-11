package edu.weather.service.location.ipstack.model;

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
    private String type;
    private String info;
}
