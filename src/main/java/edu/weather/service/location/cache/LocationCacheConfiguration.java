package edu.weather.service.location.cache;

import edu.weather.service.location.model.ILocation;
import org.apache.commons.lang3.tuple.Pair;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import javax.cache.configuration.Configuration;
import java.time.Duration;

/**
 * Location information cache configuration component.
 *
 * @author andris
 * @since 1.0.0
 */
@org.springframework.context.annotation.Configuration
public class LocationCacheConfiguration {

    public static final String CACHE_NAME = "locationCache";

    @Value("${location.cache.ttl:60}")
    private int locationCacheTTL;

    @Bean
    public Pair<String, Configuration<String, ILocation>> locationCacheConfig() {
        CacheConfigurationBuilder<String, ILocation> config =
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                        String.class,
                        ILocation.class,
                        ResourcePoolsBuilder
                                .newResourcePoolsBuilder().offheap(1, MemoryUnit.MB))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(locationCacheTTL)));

        return Pair.of(CACHE_NAME, Eh107Configuration.fromEhcacheCacheConfiguration(config));
    }
}
