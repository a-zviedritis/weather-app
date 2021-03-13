package edu.weather.rest.mapper;

import edu.weather.rest.model.WeatherDTO;
import edu.weather.service.weather.weatherapi.model.WeatherInfo;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

public class WeatherMapperTest {

    private static WeatherMapper mapper = Mappers.getMapper(WeatherMapper.class);

    @Test
    public void testToDTO() {
        WeatherInfo info = new WeatherInfo();
        info.setTemperature(1.2);
        info.setTemperatureFeelsLike(0.5);
        info.setHumidity(50);
        info.setGustSpeed(12.3);
        info.setWindSpeed(10.0);
        info.setWindDegree(100);
        info.setWindDirection("N");
        WeatherInfo.Condition condition = new WeatherInfo.Condition();
        condition.setText("Normal");
        info.setCondition(condition);

        WeatherDTO dto = mapper.toDTO(info);

        assertThat(dto).isNotNull();
        assertThat(dto.getTemperature()).isEqualTo(info.getTemperature());
        assertThat(dto.getTemperatureFeelsLike()).isEqualTo(info.getTemperatureFeelsLike());
        assertThat(dto.getHumidity()).isEqualTo(info.getHumidity());
        assertThat(dto.getGustSpeed()).isEqualTo(info.getGustSpeed());
        assertThat(dto.getWindSpeed()).isEqualTo(info.getWindSpeed());
        assertThat(dto.getWindSpeed()).isEqualTo(info.getWindSpeed());
        assertThat(dto.getWindDegree()).isEqualTo(info.getWindDegree());
        assertThat(dto.getWindDirection()).isEqualTo(info.getWindDirection());
        assertThat(dto.getCondition()).isEqualTo(info.getCondition());
    }
}
