package edu.weather.rest.mapper;

import edu.weather.rest.model.WeatherDTO;
import edu.weather.service.weather.model.IWeatherInfo;
import org.mapstruct.Mapper;

/**
 * Mapping component responsible for producing {@link WeatherDTO} instances.
 *
 * @author andris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface WeatherMapper {

    WeatherDTO toDTO(IWeatherInfo weather);
}
