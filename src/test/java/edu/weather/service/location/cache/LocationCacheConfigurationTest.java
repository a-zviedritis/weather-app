package edu.weather.service.location.cache;

import edu.weather.service.location.model.ILocation;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.cache.configuration.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationCacheConfigurationTest {

    @Test
    public void testLocationCacheConfig() {
        LocationCacheConfiguration config = new LocationCacheConfiguration();
        ReflectionTestUtils.setField(config, "locationCacheTTL", 10);

        Pair<String, Configuration<String, ILocation>> result = config.locationCacheConfig();

        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(LocationCacheConfiguration.CACHE_NAME);
    }
}
