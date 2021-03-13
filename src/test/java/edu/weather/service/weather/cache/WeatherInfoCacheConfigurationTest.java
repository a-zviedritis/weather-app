package edu.weather.service.weather.cache;

import edu.weather.service.weather.model.IWeatherInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.util.ReflectionTestUtils;

import javax.cache.configuration.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class WeatherInfoCacheConfigurationTest {

    @Test
    public void testLocationCacheConfig() {
        WeatherInfoCacheConfiguration config = new WeatherInfoCacheConfiguration();
        ReflectionTestUtils.setField(config, "weatherInfoCacheTTL", 10);

        Pair<String, Configuration<SimpleKey, IWeatherInfo>> result = config.weatherInfoCacheConfig();

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(WeatherInfoCacheConfiguration.CACHE_NAME);
    }
}
