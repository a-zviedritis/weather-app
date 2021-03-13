package edu.weather.rest.mapper;

import edu.weather.rest.model.LocationDTO;
import edu.weather.service.location.ipstack.model.LocationResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationMapperTest {

    private static final String CITY = "Riga";
    private static final String COUNTRY = "Latvia";
    private static final String CONTINENT = "Europe";
    private static final double LATITUDE = 12.34;
    private static final double LONGITUDE = 56.78;

    private static LocationMapper mapper = Mappers.getMapper(LocationMapper.class);

    @Test
    public void testToDTO() {
        LocationResponse location = new LocationResponse();
        location.setCity(CITY);
        location.setCountry(COUNTRY);
        location.setContinent(CONTINENT);
        location.setLatitude(LATITUDE);
        location.setLongitude(LONGITUDE);

        LocationDTO dto = mapper.toDTO(location);

        assertThat(dto).isNotNull();
        assertThat(dto.getCity()).isEqualTo(location.getCity());
        assertThat(dto.getCountry()).isEqualTo(location.getCountry());
        assertThat(dto.getContinent()).isEqualTo(location.getContinent());
        assertThat(dto.getLatitude()).isEqualTo(location.getLatitude());
        assertThat(dto.getLongitude()).isEqualTo(location.getLongitude());
    }
}
