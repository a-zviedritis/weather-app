package edu.weather.rest.mapper;

import edu.weather.rest.model.LocationDTO;
import edu.weather.service.location.ipstack.model.Location;
import org.mapstruct.Mapper;

/**
 * Mapping component responsible for producing {@link LocationDTO} instances.
 *
 * @author andris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDTO toDTO(Location location);
}
