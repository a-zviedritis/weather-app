package edu.weather.service.weather.cache;

import edu.weather.service.weather.model.IWeatherInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;

import javax.cache.configuration.Configuration;
import java.time.Duration;

/**
 * Weather information cache configuration component.
 *
 * @author andris
 * @since 1.0.0
 */
@org.springframework.context.annotation.Configuration
public class WeatherInfoCacheConfiguration {

    public static final String CACHE_NAME = "weatherInfoCache";

    @Value("${weather.cache.ttl:60}")
    private int weatherInfoCacheTTL;

    @Bean
    public Pair<String, Configuration<SimpleKey, IWeatherInfo>> weatherInfoCacheConfig() {
        CacheConfigurationBuilder<SimpleKey, IWeatherInfo> config =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        SimpleKey.class,
                        IWeatherInfo.class,
                        ResourcePoolsBuilder
                                .newResourcePoolsBuilder().offheap(1, MemoryUnit.MB))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(weatherInfoCacheTTL)));

        return Pair.of(CACHE_NAME, Eh107Configuration.fromEhcacheCacheConfiguration(config));
    }
}
